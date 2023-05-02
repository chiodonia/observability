# Observability

```
docker compose -f compose/broker-confluent.yml up
docker compose -f compose/observability.yml up
```

A: web-client
B: spring-kafka-consumer, spring-kafka-producer 
C: kafka-streams
D: kafka-consumer
e: kafka-producer

```
  ┌─────────┐         ┌─────────┐                ┌─────────┐                 ┌─────────┐
  │         │         │         │    ┌───────┐   │         │    ┌───────┐    │         │
  │    A    ├────────►│    B    ├───►│  t1   ├──►│    C    ├───►│  t2   ├───►│    D    │
  │  7070   │  HTTP   │  7071   │    └───────┘   │  7072   │    └───────┘    │  7073   │
  └─────────┘         └─────────┘                └─────────┘                 └─────────┘
                           ▲
                           │
  ┌─────────┐              │
  │         │    ┌───────┐ │                                  
  │    E    ├───►│  t3   ├─┘                  
┌─►  7074   │    └───────┘
│ └─┬───────┘
└───┘
```

```
curl -X POST http://localhost:7070/items/2 -d Hello-2
curl -X GET http://localhost:7070/items/2 | jq
curl -X GET http://localhost:7070/items | jq
```

```
for i in {1..10}
do
  curl -X POST http://localhost:7070/items/${i} -d Hello-${i}
done
```

```
for i in {1..1000}
do
    curl -X GET http://localhost:7070/items
done
```

http://localhost:16686

