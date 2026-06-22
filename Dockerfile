#--- 1. Construcción (compilar el .jar)---

#Usamos una imagen que trae Maven y Java 21 
FROM maven:3.9-eclipse-temurin-21 AS build 
#Definimos /app como la carpeta de trabajo dentro del contenedor
WORKDIR /app 

#Copiamos el pom.xml para descargar las dependencias necesarias
COPY pom.xml .
#Descargamos las dependencias necesarias para el proyecto
RUN mvn dependency:go-offline

#Copiamos el código fuente del proyecto al contenedor
COPY src ./src
#Compilamos y generamos el .jar en /app/target/. Saltamos los tests para que el build sea mas rápido
RUN mvn clean package -DskipTests

#--- 2. Ejecucion (correr el .jar)---

#Nueva imagen base para ejecutar el .jar, esta imagen solo trae Java 21, no trae Maven
FROM eclipse-temurin:21-jre

#Definimos /app como la carpeta de trabajo dentro del contenedor
WORKDIR /app

#Copiamos el .jar generado en la etapa de construcción a la nueva imagen para ejecución
COPY --from=build /app/target/*.jar app.jar

#Exponemos el puerto 8080 para que el contenedor pueda recibir tráfico en ese puerto
EXPOSE 8081

#Definimos el comando de entrada para ejecutar el .jar cuando se inicie el contenedor
ENTRYPOINT ["java", "-jar", "app.jar"]


