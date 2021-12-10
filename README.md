# Content Cloud Blacksmith

Artifact builder for Content Cloud

## Configuration

### Scribe

Blacksmith talks to [Scribe] to generate the project source code.

* `blacksmith.scribe.url` - Scribe endpoint. Defaults to `https://api.content-cloud.eu/codegen/`

### RabbitMQ:

* `blacksmith.rabbitmq.enabled` - Whether to enable RabbitMQ integration. Defaults to `false`
* `blacksmith.rabbitmq.exchange` - Name of the exchange to use for send operations. Defaults to `contentcloud`.

Connection with RabbitMQ can be configured with standard Spring Boot configuration properties:

* `spring.rabbitmq.host` - RabbitMQ host. Ignored if an address is set. Default to `localhost`.
* `spring.rabbitmq.port` - RabbitMQ port. Ignored if an address is set. Default to `5672`, or `5671` if SSL is enabled.
* `spring.rabbitmq.username` - Login user to authenticate to the RabbitMQ broker. Default to `guest`.
* `spring.rabbitmq.password` - Password to authenticate against the broker. Default to `guest`.

See [RabbitMQ support] in the Spring Boot documentation for more details.

[Scribe]: https://github.com/xenit-eu/contentcloud-scribe
[RabbitMQ Support]: https://docs.spring.io/spring-boot/docs/current/reference/html/messaging.html#messaging.amqp.rabbitmq]
