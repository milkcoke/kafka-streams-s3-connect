#!/bin/bash

# Path to the NDJSON file
ndjson_file=$1

if [ -z "$ndjson_file" ]; then
  echo "Target ndjson file is null or empty"
  exit 1
fi

# Kafka producer properties
bootstrap_servers="localhost:9092"
topic="retry-topic"

# Use jq to parse each line of the NDJSON file and produce it to Kafka
jq -r '. | @json' "$ndjson_file" |
kafka-console-producer.sh --broker-list "$bootstrap_servers" \
--topic "$topic" \
--producer-property value.serializer=org.apache.kafka.common.serialization.StringSerializer \
--producer-property retries=5 \
--producer-property enable.idempotence=true \
--producer-property request-required-acks=-1 \
--producer-property max.in.flight.requests.per.connection=1
