# orchestrations

Defines the orchestration and adapter interfaces ("Activities") for ecommerce.
In this demo, the bounded contexts `inventory` and `shipping` implement the `ActivityInterface` directly
but in the real world they would be passed in as dependencies into an Adapter implementation.
