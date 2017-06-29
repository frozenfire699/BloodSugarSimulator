FROM maven:3.5.0-jdk-8
WORKDIR /apps
COPY . /apps
RUN mvn clean install
CMD mvn exec:java -Dexec.mainClass="org.rohit.test.bloodsugar.simulator.SimulatorClient"
