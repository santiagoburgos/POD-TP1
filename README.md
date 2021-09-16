# TPE1: Despegues
Repositorio para TPE1 de POD 2C
## Integrantes
### Grupo 6:
* Santiago Burgos - legajo 55193
* Ximena Zuberbuhler - legajo 57287
* Cristóbal Matías Rojas - legajo 58564
* Enrique Tawara - legajo 58717
## Instalación
### Paso 0: Obtención del archivo
En caso de no disponer de los archivos obtener mediante:
```bash
git pull https://github.com/etawara/ITBA_POD_2C2021_G06_TPE1.git
```
### Paso 1: Generación de binarios
Compilar con maven los archivos en el directorio ``tpe-g6``
```bash
cd tpe1-g6 && mvn clean install && cd ..
```
### Paso 2: Obtención de binarios
Los binarios se encuentran dentro de los archivos:
* ``tpe-6/server/target/tpe1-g6-server-1.0-SNAPSHOT-bin.tar.gz`` para el servidor y registry
* ``tpe-6/client/target/tpe1-g6-client-1.0-SNAPSHOT-bin.tar.gz`` para los clientes

Descomprimir dichos archivos
```bash
tar -xzf <path al archivo .tar.gz>
```
### Paso 3: Ejecución del programa
#### Registry
```bash
cd <directorio descomprimido el tpe1-g6-server-1.0-SNAPSHOT-bin.tar.gz>
./run-registry.sh
```
#### Server
```bash
cd <directorio descomprimido el tpe1-g6-server-1.0-SNAPSHOT-bin.tar.gz>
./run-server.sh
```
#### Clientes
##### Cliente de Administración
```bash
cd <directorio descomprimido el tpe1-g6-client-1.0-SNAPSHOT-bin.tar.gz>
./run-management.sh -DserverAddress=xx.xx.xx.xx:yyyy -Daction=actionName
[ -Drunway=runwayName | -Dcategory=minCategory ]
```
##### Cliente de Solicitud de Pista
```bash
cd <directorio descomprimido el tpe1-g6-client-1.0-SNAPSHOT-bin.tar.gz>
./run-runway.sh -DserverAddress=xx.xx.xx.xx:yyyy -DinPath=fileName

```
##### Cliente de Seguimiento de Vuelo
```bash
cd <directorio descomprimido el tpe1-g6-client-1.0-SNAPSHOT-bin.tar.gz>
./run-airline.sh -DserverAddress=xx.xx.xx.xx:yyyy -Dairline=airlineName
-DflightCode=flightCode
```
##### Cliente de Consulta
```bash
cd <directorio descomprimido el tpe1-g6-client-1.0-SNAPSHOT-bin.tar.gz>
 ./run-query.sh -DserverAddress=xx.xx.xx.xx:yyyy [ -Dairline=airlineName |
-Drunway=runwayName ] -DoutPath=fileName
```