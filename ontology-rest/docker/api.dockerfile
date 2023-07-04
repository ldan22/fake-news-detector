FROM tomcat:9.0.73-jdk17 as sigmakee_builder

ENV SIGMADIR=/sigma

ENV PATH=$PATH:$JAVA_HOME/bin \
    SOURCES=$SIGMADIR/sources \
    PROGRAMS=$SIGMADIR/Programs \
    ME=root

ENV KBDIR=$SIGMA_HOME/KBs \
    SIGMA_SRC=$SOURCES/sigmakee \
    ONTOLOGYPORTAL_GIT=$SOURCES


# Install system deps
RUN apt update &&\
    apt -qq install -y ant git make gcc graphviz g++ libz-dev&&\
    rm -rf /var/lib/apt/lists/* && \
    apt clean -y &&\
    apt autoclean -y


WORKDIR $SIGMADIR

COPY ./sigma/Programs $SIGMADIR/Programs
COPY ./sigma/sources $SIGMADIR/sources

# Build Vampire
RUN cd $PROGRAMS/vampire &&\
    make vampire_rel &&\
    mv vampire_rel_master* vampire

# Build EProver
RUN cd $PROGRAMS/E &&\
    ./configure &&  \
    make &&  \
    make install

# Build sigmakee
RUN cd $SOURCES/sigmakee &&\
    ant


# Build sigmanlp
RUN cd $SOURCES/sigmanlp && \
    ant


#################################################

FROM maven:latest

# Build and deploy custom rest module
ENV SIGMADIR=/sigma
ENV ONTOLOGY_REST=/ontology-rest

ENV PATH=$PATH:$JAVA_HOME/bin \
    SOURCES=$SIGMADIR/sources \
    PROGRAMS=$SIGMADIR/Programs \
    SIGMA_HOME=$SIGMADIR/.sigmakee \
    CATALINA_OPTS="$CATALINA_OPTS -Xms2g -Xmx6g"\
    ME=root

ENV KBDIR=$SIGMA_HOME/KBs \
    SUMO_SRC=$SOURCES/sumo \
    SIGMA_SRC=$SOURCES/sigmakee \
    ONTOLOGYPORTAL_GIT=$SOURCES


WORKDIR $ONTOLOGY_REST

COPY ./src src
COPY ./lib/ lib
COPY ./pom.xml .

COPY --from=sigmakee_builder \
    $PROGRAMS/E/ $PROGRAMS/E/

COPY --from=sigmakee_builder \
    $PROGRAMS/vampire/ $PROGRAMS/vampire/

COPY --from=sigmakee_builder \
    $PROGRAMS/stanford-corenlp-4.5.4/ $PROGRAMS/stanford-corenlp-4.5.4/

COPY --from=sigmakee_builder \
    $SOURCES/sigmakee/build/sigmakee.jar $ONTOLOGY_REST/lib

COPY --from=sigmakee_builder \
    $SOURCES/sigmanlp/build/sigmanlp.jar $ONTOLOGY_REST/lib

COPY --from=sigmakee_builder \
    $SOURCES/sumo/ $SOURCES/sumo/

RUN mvn clean package

ENTRYPOINT ["java", "-Xmx6g", "--add-opens", "java.base/java.lang=ALL-UNNAMED", "--add-opens", "java.base/java.math=ALL-UNNAMED", "--add-opens", "java.base/java.util=ALL-UNNAMED", "--add-opens", "java.base/java.util.concurrent=ALL-UNNAMED", "--add-opens", "java.base/java.net=ALL-UNNAMED", "--add-opens", "java.base/java.text=ALL-UNNAMED", "--add-opens", "java.sql/java.sql=ALL-UNNAMED", "-Dloader.path=/sigma/Programs/stanford-corenlp-4.5.4", "-jar", "/ontology-rest/target/ontology-rest.jar"]
#ENTRYPOINT ["java", "-Xmx6g", "--add-opens", "java.base/java.lang=ALL-UNNAMED", "--add-opens", "java.base/java.math=ALL-UNNAMED", "--add-opens", "java.base/java.util=ALL-UNNAMED", "--add-opens", "java.base/java.util.concurrent=ALL-UNNAMED", "--add-opens", "java.base/java.net=ALL-UNNAMED", "--add-opens", "java.base/java.text=ALL-UNNAMED", "--add-opens", "java.sql/java.sql=ALL-UNNAMED", "-Dloader.path=/sigma/Programs/stanford-corenlp-4.5.4", "-cp", "/ontology-rest/target/ontology-rest.jar", "org.springframework.boot.loader.PropertiesLauncher"]
