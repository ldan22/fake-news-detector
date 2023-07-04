FROM tomcat:9.0.73-jdk17

# Build and deploy sigmakee
ENV SIGMADIR=/sigma
ENV ONTOLOGY_REST=/ontology-rest

ENV PATH=$PATH:$JAVA_HOME/bin \
    SOURCES=$SIGMADIR/sources \
    PROGRAMS=$SIGMADIR/Programs \
    SIGMA_HOME=$SIGMADIR/.sigmakee \
    CATALINA_OPTS="$CATALINA_OPTS -Xms1000M -Xmx5G"\
    ME=root

ENV KBDIR=$SIGMA_HOME/KBs \
    SUMO_SRC=$SOURCES/sumo \
    SIGMA_SRC=$SOURCES/sigmakee \
    ONTOLOGYPORTAL_GIT=$SOURCES

# Port for sigmakee
EXPOSE 8080

WORKDIR $SIGMADIR

COPY ./sigma/Programs $SIGMADIR/Programs
COPY ./sigma/sources $SIGMADIR/sources

# Install system deps
RUN apt update &&\
    apt -qq install -y ant git make gcc graphviz &&\
    rm -rf /var/lib/apt/lists/* && \
    apt clean -y &&\
    apt autoclean -y


# Make EProver
RUN cd $PROGRAMS/E &&\
    ./configure && make && make install

# Build sigmakee
RUN cd $SIGMA_SRC &&\
    ant

## Copy dependencies for sigmanlp
RUN cd $PROGRAMS/stanford-corenlp-4.5.4 && \
    cp ./stanford-corenlp-4.5.4.jar $SOURCES/sigmanlp/lib && \
    cp ./stanford-corenlp-4.5.4-models.jar $SOURCES/sigmanlp/lib


# Build sigmanlp
RUN cd $SOURCES/sigmanlp && \
    ant && \
    ant dist
