# Not&Notion

Una aplicación Android que combina las funcionalidades de **notas** y **calendario** en una sola app. Permite crear notas, eventos y recordatorios, facilitando la organización personal de manera rápida y eficiente.

---

## Objetivo del proyecto

El objetivo principal de esta aplicación es **unificar la gestión de notas y eventos** para ofrecer al usuario una experiencia integrada. Esto incluye:

- Crear, editar y eliminar notas y carpetas.
- Agregar eventos y recordatorios en el calendario.
- Visualizar tareas y recordatorios de forma sencilla en un solo lugar.
- Gestión segura de usuarios con login, registro y perfil de usuario.

---

## Flujo de la aplicación

### 1. **Login**
- Iniciar sesión con **usuario y contraseña** o con **Google**.
- Botón para **crear una nueva cuenta**.
- La sesión permanece activa hasta que el usuario cierre sesión manualmente.

### 2. **Registro**
- Introducir **nombre, correo electrónico, contraseña y confirmar contraseña**.
- Botón para **volver a login** si el usuario ya tiene cuenta.
- Una vez registrado, se puede iniciar sesión y acceder a la aplicación.

### 3. **Pantalla principal: Notas**
- Barra de búsqueda para buscar notas o carpetas.
- Menú inferior para navegar entre **Notas** y **Calendario**.
- Botón **Volver atrás** para navegar entre carpetas (estilo explorador).
- Botón **Añadir** para crear:
  - Nueva nota.
  - Nueva carpeta.
- Botón **Perfil** para acceder a la pantalla de perfil del usuario.

### 4. **Pantalla de Edición de Nota**
- Al pulsar una nota existente, se abre esta pantalla.
- Permite editar:
  - **Título** de la nota.
  - **Descripción** del contenido.
  - **Tamaño del texto**.
  - Estilos: **negrita, cursiva, subrayado**.
  - Añadir una **portada** que será la miniatura en la pantalla principal.
- Botón **Añadir** para adjuntar:
  - **Imagen**.
  - **Documento**.
- Botón **Volver atrás** para salir sin guardar cambios.
- Botón **Guardar** para aplicar cambios en la nota.

### 5. **Pantalla de Perfil**
- Botón para **cerrar sesión**.
- Botón para **editar perfil**, que abre la pantalla de edición de perfil.

### 6. **Pantalla de Edición de Perfil**
- Cambiar **nombre de usuario**.
- Editar **foto de perfil**.
- Actualizar **contraseña** (requiere contraseña actual y nueva).
- Botón **Volver atrás** para salir sin guardar cambios.

### 7. **Pantalla de Calendario**
- Menú inferior para navegar entre **Notas** y **Calendario**.
- Botón **Perfil** para abrir la pantalla de perfil.
- Botón **Añadir** para crear un nuevo evento.
- Calendario mensual en la parte superior.
- Lista de eventos del día seleccionado en la parte inferior (muestra título y hora).

### 8. **Pantalla de Añadir Evento**
- EditText para **título del evento**.
- EditText para **breve descripción del evento**.
- Selector de **hora** (00:00 por defecto).
- Botón **Añadir Recordatorio** para configurar recordatorios asociados al evento.
- Guardar evento y mostrarlo en la pantalla de calendario.

### 9. **Pantalla de Edición de Evento**
- Al pulsar un evento existente, se abre esta pantalla.
- Permite editar:
  - **Nombre del evento**
  - **Fecha**
  - **Hora**
  - **Recordatorio**
- Botón **Guardar cambios** para actualizar el evento.
- Botón **Eliminar** para borrar el evento por completo.
- Botón **Volver atrás** para salir sin guardar cambios.

---

## Requisitos mínimos para ejecutar el proyecto

### Android SDK
- `minSdkVersion`: 24 (Android 7.0 Nougat)  
- `targetSdkVersion`: 36  
- `compileSdkVersion`: 36  

### Java
- `sourceCompatibility` y `targetCompatibility`: **Java 11**

### Android Gradle Plugin
- `agp`: 8.11.2

### Dependencias principales
- **JUnit (unit tests):** 4.13.2  
- **AndroidX Test (JUnit Runner):** 1.3.0  
- **Espresso (UI testing):** 3.7.0  
- **AppCompat:** 1.7.1  
- **Material Components:** 1.13.0  
- **AndroidX Activity:** 1.11.0  
- **ConstraintLayout:** 2.2.1  

💡 **Nota:** Para compilar y ejecutar este proyecto necesitas:
- Android Studio compatible con AGP 8.11.2  
- SDK de Android 36  
- JDK 11

---

## Uso básico

1. **Login**
   - Ingresa con usuario y contraseña o con tu cuenta de Google.
   - Botón para crear nueva cuenta si aún no estás registrado.
   - La sesión permanece activa hasta que cierres sesión manualmente.

2. **Registro**
   - Introduce nombre, correo electrónico, contraseña y confirma contraseña.
   - Botón para volver a login si ya tienes cuenta.
   - Luego inicia sesión con tus credenciales nuevas.

3. **Pantalla de Notas**
   - Barra de búsqueda para encontrar notas y carpetas.
   - Menú inferior para navegar entre **Notas** y **Calendario**.
   - Botón “Volver atrás” para navegar entre carpetas.
   - Botón “Añadir” para crear notas o carpetas nuevas.
   - Botón “Perfil” para acceder a la pantalla de perfil.

4. **Pantalla de Edición de Nota**
   - Editar título y descripción.
   - Cambiar tamaño de texto y aplicar estilos: negrita, cursiva, subrayado.
   - Añadir portada como miniatura.
   - Botón “Añadir” para insertar imágenes o documentos.
   - Botón “Volver atrás” para salir sin guardar cambios.
   - Botón “Guardar” para aplicar cambios.

5. **Pantalla de Perfil**
   - Botón para cerrar sesión.
   - Botón para editar perfil, que abre la pantalla de edición de datos del usuario.

6. **Pantalla de Edición de Perfil**
   - Cambiar nombre de usuario.
   - Editar foto de perfil.
   - Actualizar contraseña (requiere contraseña actual y nueva).
   - Botón “Volver atrás” para salir sin guardar cambios.

7. **Pantalla de Calendario**
   - Menú inferior para navegar entre **Notas** y **Calendario**.
   - Botón “Perfil” para abrir la pantalla de perfil.
   - Botón “Añadir” para crear un nuevo evento.
   - Calendario mensual en la parte superior.
   - Lista de eventos del día seleccionado en la parte inferior (muestra título y hora).

8. **Pantalla de Añadir Evento**
   - EditText para título del evento.
   - EditText para breve descripción del evento.
   - Selector de hora (00:00 por defecto).
   - Botón “Añadir Recordatorio” para configurar recordatorios asociados al evento.
   - Guardar evento y mostrarlo en la pantalla de calendario.

9. **Pantalla de Edición de Evento**
   - Editar nombre, fecha, hora y recordatorio del evento.
   - Botón “Guardar cambios” para actualizar el evento.
   - Botón “Eliminar” para borrar el evento.
   - Botón “Volver atrás” para salir sin guardar cambios.

---
