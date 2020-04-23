FROM adoptopenjdk/openjdk11
ADD build/skylis.jar skylis.jar
ADD build/application.properties application.properties
ADD build/private.pem private.pem
ADD build/public.pem public.pem
ADD build/private-pkcs8.pem private-pkcs8.pem.pem
ENTRYPOINT ["java", "-jar", "skyplus.jar"]
EXPOSE 8288
