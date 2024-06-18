#!/bin/bash

mvn clean install -DskipTests

docker compose --env-file .env.production.local up -d geospatial-data-source tiles-api-app --build

docker compose --env-file .env.production.local up -d dashboard-app --build

