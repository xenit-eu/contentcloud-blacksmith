package eu.xenit.contentcloud.blacksmith.infra.build.gradle;

import eu.xenit.contentcloud.blacksmith.infra.build.BuildFailedException;
import eu.xenit.contentcloud.blacksmith.model.artifact.ArtifactDescriptor;
import eu.xenit.contentcloud.blacksmith.model.publication.DockerImageRegistry;
import eu.xenit.contentcloud.blacksmith.util.logging.Slf4jOutputStream;
import lombok.extern.slf4j.Slf4j;
import org.gradle.tooling.GradleConnectionException;
import org.gradle.tooling.GradleConnector;
import org.gradle.tooling.ProjectConnection;
import org.gradle.tooling.ResultHandler;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

@Slf4j
public class GradleBuildExecutor {

    private static final String INITSCRIPT = "gradle/blacksmith.gradle";

    public CompletableFuture<ArtifactDescriptor> publishDockerImage(Path workingDirectory, DockerImageRegistry registry) {
        return this.build(workingDirectory, "bootBuildImage", registry);
    }

    private CompletableFuture<ArtifactDescriptor> build(Path workingDirectory, String task, DockerImageRegistry registry) {
        try {
            ProjectConnection connection = GradleConnector.newConnector()
                    .forProjectDirectory(workingDirectory.toFile())
                    .connect();

            CompletableFuture<ArtifactDescriptor> future = new CompletableFuture<>();
            connection.newBuild()
                    .setStandardOutput(new Slf4jOutputStream(log, Slf4jOutputStream.LogLevel.INFO))
                    .forTasks(task)
                    .withArguments(new GradleLaunchArguments()
                            .withInitScript(gradleInitScript())
                            .addProperty("eu.xenit.contentcloud.docker.registry.name", registry.getName())
                            .addProperty("eu.xenit.contentcloud.docker.registry.url", registry.getUrl().toString())
                            .addPropertyIf(registry.getUsername() != null,
                                    "eu.xenit.contentcloud.docker.registry.username", registry.getUsername())
                            .addPropertyIf(registry.getPassword() != null,
                                    "eu.xenit.contentcloud.docker.registry.password", registry.getPassword())
                            .addPropertyIf(registry.getToken() != null,
                                    "eu.xenit.contentcloud.docker.registry.token", registry.getToken())
                    )
                    .run(new ResultHandler<>() {
                        @Override
                        public void onComplete(Void result) {
                            try {
                                String dockerImage = Files.readString(workingDirectory.resolve("build").resolve("docker.image"));
                                var artifact = new ArtifactDescriptor("docker-image", dockerImage);
                                future.complete(artifact);

                            } catch (IOException e) {
                                e.printStackTrace();
                            } finally {
                                connection.close();
                            }
                        }

                        @Override
                        public void onFailure(GradleConnectionException failure) {
                            try {
                                log.info("build failed", failure);
                                BuildFailedException buildFailed = new BuildFailedException("Build or publish docker image failed", failure);
                                buildFailed.fillInStackTrace();

                                future.completeExceptionally(buildFailed);
                            } finally {
                                connection.close();
                            }
                        }
                    });


            return future;
        } catch (GradleConnectionException gce) {
            return CompletableFuture.failedFuture(gce);
        }
    }

    static String gradleInitScript() {
        try {
            Path gradleInitScriptPath = Files.createTempFile("smith-", ".gradle");

            try (InputStream stream = GradleBuildExecutor.class.getClassLoader().getResourceAsStream(INITSCRIPT)) {
                Objects.requireNonNull(stream, "stream not found: " + INITSCRIPT);
                Files.copy(stream, gradleInitScriptPath, StandardCopyOption.REPLACE_EXISTING);
            }

            return gradleInitScriptPath.toAbsolutePath().toString();

        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }


    }

    class GradleLaunchArguments implements Iterable<String> {

        private final List<String> args = new ArrayList<>();

        public GradleLaunchArguments withInitScript(String gradleInitScript) {
            return this.addArguments("--init-script", gradleInitScript());
        }


        public GradleLaunchArguments addProperty(String key, String value) {
            return this.addArguments(String.format("-P%s=%s", key, value));
        }

        public GradleLaunchArguments addPropertyIf(boolean guard, String key, String value) {
            if (guard) {
                this.addProperty(key, value);
            }
            return this;
        }


        public GradleLaunchArguments addArguments(String... arguments) {
            args.addAll(Arrays.asList(arguments));
            return this;
        }

        @Override
        public Iterator<String> iterator() {
            return this.args.iterator();
        }
    }
}
