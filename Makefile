.PHONY: build test verify format run docker-up docker-down docker-reset

build:
	./mvnw clean compile

test:
	./mvnw test

verify:
	./mvnw clean verify

format:
	./mvnw spotless:apply

run:
	./mvnw spring-boot:run

docker-up:
	docker compose up --build

docker-down:
	docker compose down

# Borra también el volumen de Postgres: se pierden todos los datos.
docker-reset:
	docker compose down -v
