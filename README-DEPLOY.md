# Despliegue de API_HealthBlood_512 en Render

Esta guía deja la API pública en Render usando:

- **Render Web Service Free** con Docker.
- **Tomcat 11 + JDK 21**.
- **Filess.io MySQL 8** gratuito como base de datos.
- **CORS abierto** para que un frontend web pueda consumir la API.

## Arquitectura

```text
Internet
   |
   v
Render Web Service Free (Docker)
Tomcat 11 + WAR en la raiz
   |
   | TLS por Internet
   v
Filess.io MySQL 8
```

La API queda en:

```text
https://<servicio>.onrender.com/api/glucosa/...
```

## 1. Crear la base de datos en Filess.io

1. Entra a <https://panel.filess.io/register> y crea una cuenta.
2. Crea una base **MySQL 8** en el plan **Free**.
   - El plan Free incluye 2 bases y 10 MB por base.
   - No requiere tarjeta.
3. Copia los datos de conexión:
   - Host
   - Puerto
   - Nombre de la base
   - Usuario
   - Contraseña
4. Ejecuta el esquema `db/schema-cloud.sql` sobre esa base.

Puedes usar el editor SQL del panel de Filess.io o un cliente MySQL como DBeaver o MySQL Workbench.

Con el cliente `mysql`:

```bash
mysql -h <host> -P <puerto> -u <usuario> -p --ssl-mode=REQUIRED <base_de_datos> < db/schema-cloud.sql
```

El esquema crea las tablas `usuario` y `medida`. No crea `FOREIGN KEY` para mantener compatibilidad con TiDB; la API valida el usuario antes de guardar una medida.

## 2. Probar la imagen Docker localmente

Copia `.env.example` a `.env` y coloca los datos reales:

```powershell
Copy-Item .env.example .env
notepad .env
```

Construye la imagen:

```powershell
docker build -t healthblood-api .
```

Ejecuta el contenedor:

```powershell
docker run --rm -p 10000:10000 --env-file .env healthblood-api
```

Prueba el endpoint de salud:

```powershell
curl.exe http://localhost:10000/api/glucosa/saludar
```

Respuesta esperada:

```json
{ "response" : "Hola desde Java" }
```

> Si el puerto 10000 está ocupado, usa otro puerto local:
> `docker run --rm -p 8080:10000 --env-file .env healthblood-api`

## 3. Subir el proyecto a GitHub

Crea un repositorio vacío en GitHub llamado `API_HealthBlood_512` y ejecuta:

```powershell
git init
git add .
git commit -m "Preparar despliegue en Render"
git branch -M main
git remote add origin https://github.com/<tu-usuario>/API_HealthBlood_512.git
git push -u origin main
```

Antes de hacer `git add`, verifica que `.env` no aparezca en `git status`. El archivo `.gitignore` ya excluye `build/`, `dist/` y los archivos `.env`.

## 4. Crear el servicio en Render

### Opción A: Blueprint (recomendado)

1. En Render, entra a **New > Blueprint**.
2. Conecta el repositorio de GitHub.
3. Render detecta `render.yaml`.
4. Completa las variables que se solicitan:
   - `DB_HOST`
   - `DB_PORT`
   - `DB_NAME`
   - `DB_USER`
   - `DB_PASSWORD`
5. Aplica el Blueprint.

`DB_SSL_MODE` ya viene configurado como `REQUIRED`.

### Opción B: Web Service manual

1. En Render, entra a **New > Web Service**.
2. Conecta el repositorio.
3. Configura:
   - **Language**: Docker
   - **Branch**: main
   - **Region**: Oregon
   - **Plan**: Free
   - **Health Check Path**: `/api/glucosa/saludar`
4. Agrega las variables de entorno:

| Variable | Valor |
|---|---|
| `DB_HOST` | Host de Filess.io |
| `DB_PORT` | Puerto de Filess.io |
| `DB_NAME` | Nombre de la base |
| `DB_USER` | Usuario de Filess.io |
| `DB_PASSWORD` | Contraseña de Filess.io |
| `DB_SSL_MODE` | `REQUIRED` |

5. Haz clic en **Deploy**.

Render asigna automáticamente la variable `PORT`; no la configures manualmente.

## 5. Probar los endpoints públicos

Sustituye `<servicio>` por el nombre que te asignó Render.

### Saludar

```powershell
curl.exe https://<servicio>.onrender.com/api/glucosa/saludar
```

### Crear usuario

```powershell
curl.exe -X POST "https://<servicio>.onrender.com/api/glucosa/crearUsuario" `
  -H "Content-Type: application/x-www-form-urlencoded" `
  --data-urlencode "nombre=Ana" `
  --data-urlencode "primerApellido=Lopez" `
  --data-urlencode "segundoApellido=Perez" `
  --data-urlencode "email=ana@example.com"
```

Respuesta esperada: `201 Created` con el usuario y su `idUsuario`.

### Registrar medida

Usa el `idUsuario` devuelto por el paso anterior:

```powershell
curl.exe -X POST "https://<servicio>.onrender.com/api/glucosa/registrarMedida" `
  -H "Content-Type: application/x-www-form-urlencoded" `
  --data-urlencode "idUsuario=1" `
  --data-urlencode "glucosa=105" `
  --data-urlencode "contexto=En ayunas" `
  --data-urlencode "notas=Despues de caminar"
```

Si omites `fechaHora`, el servidor usa la fecha y hora actual.

### Consultar medidas

```powershell
curl.exe https://<servicio>.onrender.com/api/glucosa/getAllMedidas
curl.exe "https://<servicio>.onrender.com/api/glucosa/getMedidasPorUsuario?idUsuario=1"
curl.exe https://<servicio>.onrender.com/api/glucosa/getAllUsuarios
```

### Casos de error

```powershell
# Usuario inexistente
curl.exe -X POST "https://<servicio>.onrender.com/api/glucosa/registrarMedida" `
  -H "Content-Type: application/x-www-form-urlencoded" `
  --data-urlencode "idUsuario=999" `
  --data-urlencode "glucosa=100"

# Glucosa fuera de rango
curl.exe -X POST "https://<servicio>.onrender.com/api/glucosa/registrarMedida" `
  -H "Content-Type: application/x-www-form-urlencoded" `
  --data-urlencode "idUsuario=1" `
  --data-urlencode "glucosa=700"
```

## 6. Probar CORS

```powershell
curl.exe -i -X OPTIONS "https://<servicio>.onrender.com/api/glucosa/crearUsuario" `
  -H "Origin: http://localhost:3000" `
  -H "Access-Control-Request-Method: POST"
```

La respuesta debe incluir:

```text
Access-Control-Allow-Origin: *
Access-Control-Allow-Methods: GET, POST, OPTIONS, HEAD
Access-Control-Allow-Headers: Content-Type, Accept, Origin, X-Requested-With
```

## 7. Configuración local

La conexión conserva los valores locales por defecto:

```text
127.0.0.1:3306 / gluco_api / root / root
```

Por eso el proyecto sigue funcionando desde NetBeans/Tomcat sin definir variables de entorno.

Si quieres probar Tomcat local contra Filess.io, define las variables antes de iniciar el servidor:

```powershell
$env:DB_HOST = "..."
$env:DB_PORT = "..."
$env:DB_NAME = "..."
$env:DB_USER = "..."
$env:DB_PASSWORD = "..."
$env:DB_SSL_MODE = "REQUIRED"
```

## Limitaciones y notas

- El Web Service Free de Render se duerme después de 15 minutos sin tráfico. La primera petición puede tardar alrededor de 1 minuto.
- Filess.io Free ofrece 10 MB por base. Es suficiente para este proyecto.
- Filess.io puede suspender la base por inactividad; la primera consulta puede tardar unos segundos.
- No hay autenticación: los endpoints de escritura son públicos.
- CORS está abierto a cualquier origen. Si más adelante quieres restringirlo, cambia `ALLOW_ORIGIN` en `CorsFilter.java`.
- Para cambiar de proveedor de base de datos solo se modifican las variables `DB_*`; el código no cambia.

## Solución de problemas

| Síntoma | Revisa |
|---|---|
| `Communications link failure` | Host, puerto y que la base de Filess.io esté activa. |
| `Access denied for user` | Usuario y contraseña; algunos proveedores incluyen un prefijo en el usuario. |
| `Unknown database` | `DB_NAME` debe ser exactamente el nombre de la base. |
| `Public Key Retrieval is not allowed` | La URL ya incluye `allowPublicKeyRetrieval=true`. |
| `SSL connection is required` | Usa `DB_SSL_MODE=REQUIRED`. |
| La API no responde en Render | Revisa los logs; Tomcat debe escuchar en `0.0.0.0:$PORT`. |
| `404` en los endpoints | La URL debe incluir `/api/...`; el WAR se publica como `ROOT`. |
| CORS falla | Verifica que `CorsFilter` esté en `org.lasalle.rest` y que Jersey escanee ese paquete. |

## Estructura agregada

```text
pom.xml
Dockerfile
docker-entrypoint.sh
render.yaml
.env.example
db/schema-cloud.sql
src/java/org/lasalle/rest/CorsFilter.java
```

El proyecto NetBeans con Ant sigue siendo válido; Maven solo se usa para construir la imagen Docker.
