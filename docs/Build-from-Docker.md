# Run cBioPortal using Docker

To learn more on Docker, kindly refer here: [What is Docker?](https://www.docker.com/what-docker).

## 1. Install Docker

First, make sure that you have the latest version of Docker installed on your machine.    
[Get latest Docker version](https://www.docker.com/products/docker)

## 2. Database Setup

### 2.1 Setup a Docker MySQL instance

#### 2.1.1 Setup a Docker Network

Because MySQL and cBioPortal are running on separate containers, Docker needs to know how to link them. Using Docker's legacy --link flag tends to be fragile since it will break if the MySQL container is restarted. We can get around this by using the newer *‘Docker networks’* feature.

```bash
docker network create "{DOCKER_NETWORK_NAME}"
```
Where `{DOCKER_NETWORK_NAME}` is the name of the network that cBioPortal and the cBioPortal DB are going to be accessible.

i.e If the network is called "cbioportal_network" the command should be:

```bash
docker network create "cbioportal_network"
```

Running the above command will create a docker network called "cbioportal_network".

#### 2.1.2 Start a MySQL docker container:

##### 2.1.2.1 Download Seed Database

You can [download the seed DB here](Downloads.md#seed-database)

##### 2.1.2.2 Run the cBioPortal DB Container
In the following command, replace `/PATH/TO/cbioportal-seed.sql.gz` by the local filename of the seed database file `cbioportal-seed.sql.gz` on the host machine (you can [download the seed DB here](Downloads.md#seed-database)). This will automatically import it before starting the MySQL server if the database does not yet exist, which may take a while.

```bash
docker run -d --name "{CONTAINER_NAME}" \
    --restart=always \
    --net="{DOCKER_NETWORK_NAME}" \
    -p {PREFERRED_EXTERNAL_PORT}:3306 \
    -e MYSQL_ROOT_PASSWORD={MYSQL_ROOT_PASSWORD} \
    -e MYSQL_USER={MYSQL_USER} \
    -e MYSQL_PASSWORD={MYSQL_PASSWORD} \
    -v {/PATH/TO/cbioportal-seed.sql.gz}:/docker-entrypoint-initdb.d/cbioportal-seed.sql.gz:ro \
    mysql
```

Where:
- `{CONTAINER_NAME}`: The name of your container instance i.e cbio_DB
- `{DOCKER_NETWORK_NAME}`: The name of your network i.e cbioportal_network
- `{PREFERRED_EXTERNAL_PORT}`: The port that the container internal port will be mapped to i.e 8306
- `{MYSQL_ROOT_PASSWORD}`: The root password for the MySQL installation. For password restrictions please read carefully this [link](http://dev.mysql.com/doc/refman/5.7/en/user-names.html)
- `{MYSQL_USER}`: The MySQL user name i.e cbio_user
- `{MYSQL_PASSWORD}`: The MySQL user password i.e P@ssword1 . For password restrictions please read carefully this [link](http://dev.mysql.com/doc/refman/5.7/en/user-names.html)
- `{MYSQL_DATABASE}`: The MySQL Database Name i.e cbioportal
- `{/PATH/TO/cbioportal-seed.sql.gz}`: The actual absolute filepath were the cbioportal-seed.sql.gz file is stored on the machine that has docker installed.

Running the above command will create a MySQL docker container and will automatically import the Seed Database.
Please note that the Seed Database import can take some time.

You can check the status of MySQL using the _Kitematic_ tool that comes with the Docker Toolbox. Or simply run:

```bash
docker ps
```

to see if the container is running and

```bash
docker logs cbioDB
```

to see the MySQL status logs.

[MySQL Docker Hub](https://hub.docker.com/_/mysql/)
[MySQL Docker Github](https://github.com/docker-library/docs/tree/master/mysql)

### 2.2 Setup a MySQL instance running on a host

#### 2.2.1 Install MySQL

To install MySQL kindly follow the installation instructions below:
[Installing and Upgrading MySQL](http://dev.mysql.com/doc/refman/5.7/en/installing.html)

#### 2.2.2 Create cBioPortal MySQL Databases and User

Once MySQL is installed on host please follow the configuration instructions below:
[Create the cBioPortal MYSQL Databases and User](https://github.com/cBioPortal/cbioportal/blob/master/docs/Pre-Build-Steps.md#create-the-cbioportal-mysql-databases-and-user)

## 3. Prepare Configuration files (Pending)

Coming soon...
- portal.properties (Placed in cbio_config)
- log4j.properties (Placed in cbio_config)
- context.xml (Placed in cbio_config)
- settings.xml (Placed in cbio_config)
- gene_sets.txt (optional) (Placed in cbio_config)
- Logos (optional)

## 4. Run the cBioPortal docker container (Pending)

```bash
docker run -d --name "{CONTAINER_NAME}" \
    --restart=always \
    --net={DOCKER_NETWORK_NAME} \
    -p {PREFERRED_EXTERNAL_PORT}:8080 \
    -v {/PATH/TO/CONFIG/}:/cbio_config/:ro \
    -v {/PATH/TO/CUSTOMIZATION}:/cbio_customization/:ro \
    -v {/PATH/TO/LOGS}:/cbio_logs/ \
    -v {/PATH/TO/STUDIES}:/cbio_studies/:ro \
    cbioportal/cbioportal:{CBIOPORTAL_VERSION}
```

Where:
- `{CONTAINER_NAME}`: The name of your container instance, i.e cbio_DB.
- `{DOCKER_NETWORK_NAME}`: The name of your network, i.e cbioportal_network.
- `{PREFERRED_EXTERNAL_PORT}`: The port that the container internal port will be mapped to, i.e 8306.
- `{/PATH/TO/CONFIG/}`: The external path were configuration files are stored.
- `{/PATH/TO/CUSTOMIZATION}`: The external path were customization files are stored.
- `{/PATH/TO/LOGS}`: The external path where you want cBioPortal Logs to be stored.
- `{/PATH/TO/STUDIES}`: The external path where cBioPortal studies are stored.
- `{CBIOPORTAL_VERSION}`: The cBioPortal Version that you would like to run, i.e latest

# Importing Studies (Pending)

Access the container interactively using the following command:

```bash
docker exec -it "{CONTAINER_NAME}" /bin/bash
```

Where:
- `{CONTAINER_NAME}`: The name of your container instance, i.e cbio_DB.

Then import the study by running:

```bash
python core/src/main/scripts/importer/metaImport.py -s {CANCER_STUDY_DIRECTORY} -u "http://localhost:8080/cbioportal" -jar core/target/core-1.3.0-SNAPSHOT.jar -o
```

Where:
- `{CANCER_STUDY_DIRECTORY}`: Is the absolute path where the cancer study is stored.

# Docker Container Maintenance

## 1. Restart Docker Container

```bash
docker restart "{CONTAINER_NAME}"
```

Where:
- `{CONTAINER_NAME}`: The name of your container instance, i.e cbio_DB.

## 2. Stop Docker Container

```bash
docker stop "{CONTAINER_NAME}"
```

Where:
- `{CONTAINER_NAME}`: The name of your container instance, i.e cbio_DB.

## 3. Remove Docker Container (Stop Docker container first)

```bash
docker rm "{CONTAINER_NAME}"
```
Where:
- `{CONTAINER_NAME}`: The name of your container instance, i.e cbio_DB.

## 4. Remove Docker Container (If docker container is unresponsive)

```bash
docker rm -fv "{CONTAINER_NAME}"
```

Where:
- `{CONTAINER_NAME}`: The name of your container instance, i.e cbio_DB.
