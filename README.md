# Flink with GCP PubSub


## Credentials

Use environment variable pointing to the GCP JSON key file or change code to pass key file path explicitly:

```bash
export GOOGLE_APPLICATION_CREDENTIALS=<path to your key file>
```

## Arguments

Main class arguments: 

```bash
<GCP project ID> <pubsub source subscription> <pubsub sink topic> <local mode flag>.
```

The last argument is a boolean type which means:
- true - runs in Flink local mode
- false or missing value - run in Flink application cluster

## Local Execution

```bash
sbt 
> run <GCP project ID> <pubsub source subscription> <pubsub sink topic>
```


## App Cluster Execution

First build fat JAR file via:

```bash
sbt 
> assembly
```

Upload it to your Flink platform as artifact.

Also, add Scala libraries in to Flink application classpath:

- scala-library-2.13.16.jar
- scala-library_3-3.3.5.jar
- scala-reflect-2.13.16.jar


## Live Testing

Put a JSON message to the source topic, for example:

```json
{
  "fullName": "John Doe",
  "birthDate": "1995-01-01"
}
```