# PuntoFácil - Sistema de Punto de Venta Android

Un sistema de punto de venta moderno y completo desarrollado en Android con Jetpack Compose, diseñado para pequeñas y medianas empresas.

## 🚀 Características Principales

### 📱 Interfaz de Usuario
- **Diseño Moderno**: Interfaz construida con Jetpack Compose y Material Design 3
- **Navegación Intuitiva**: Sistema de navegación fluido entre pantallas
- **Responsive**: Adaptable a diferentes tamaños de pantalla
- **Tema Personalizado**: Colores y tipografía optimizada para uso comercial

### 🔐 Autenticación y Seguridad
- **Sistema de Login**: Autenticación segura de usuarios
- **Gestión de Sesiones**: Control de sesiones de usuario
- **Roles de Usuario**: Diferentes niveles de acceso (Admin, Empleado)

### 💰 Gestión de Ventas
- **Punto de Venta**: Interfaz intuitiva para procesar ventas
- **Búsqueda de Productos**: Búsqueda rápida y eficiente
- **Carrito de Compras**: Gestión de items en la venta actual
- **Cálculo Automático**: Totales y subtotales automáticos
- **Historial de Ventas**: Registro completo de todas las transacciones

### 📦 Gestión de Inventario
- **Catálogo de Productos**: Gestión completa de productos
- **Control de Stock**: Seguimiento de inventario en tiempo real
- **Alertas de Stock Bajo**: Notificaciones automáticas
- **Categorización**: Organización por categorías
- **Ajustes de Inventario**: Herramientas para ajustar stock

### 👥 Gestión de Clientes
- **Base de Datos de Clientes**: Información completa de clientes
- **Historial de Compras**: Seguimiento de compras por cliente
- **Estados de Cliente**: Gestión de clientes activos/inactivos
- **Búsqueda Avanzada**: Filtros y búsqueda eficiente

### 📊 Reportes y Análisis
- **Dashboard Ejecutivo**: Métricas clave del negocio
- **Reportes de Ventas**: Análisis detallado de ventas
- **Rendimiento de Productos**: Productos más vendidos
- **Análisis de Clientes**: Insights sobre comportamiento de clientes
- **Filtros Temporales**: Reportes por día, semana, mes, año

### ⚙️ Configuración
- **Configuración del Negocio**: Información de la empresa
- **Gestión de Usuarios**: Administración de empleados
- **Respaldo de Datos**: Exportación e importación de datos
- **Configuración de Impuestos**: Gestión de tasas de impuestos
- **Personalización de Recibos**: Configuración de formato de recibos

## 🏗️ Arquitectura Técnica

### Tecnologías Utilizadas
- **Lenguaje**: Kotlin
- **UI Framework**: Jetpack Compose
- **Arquitectura**: MVVM (Model-View-ViewModel)
- **Inyección de Dependencias**: Hilt/Dagger
- **Base de Datos**: Room (SQLite)
- **Navegación**: Navigation Compose
- **Gestión de Estado**: StateFlow y Compose State

### Estructura del Proyecto

```
com.puntofacil/
├── data/
│   ├── database/
│   │   ├── entities/          # Entidades de Room
│   │   ├── dao/              # Data Access Objects
│   │   └── AppDatabase.kt    # Configuración de base de datos
│   ├── repositories/         # Repositorios de datos
│   └── di/                   # Módulos de inyección de dependencias
├── ui/
│   ├── screens/
│   │   ├── auth/            # Pantallas de autenticación
│   │   ├── dashboard/       # Panel principal
│   │   ├── sales/           # Punto de venta
│   │   ├── inventory/       # Gestión de inventario
│   │   ├── customers/       # Gestión de clientes
│   │   ├── reports/         # Reportes y análisis
│   │   └── settings/        # Configuración
│   └── theme/               # Tema y estilos
├── MainActivity.kt          # Actividad principal
└── PuntoFacilApplication.kt # Clase de aplicación
```

### Base de Datos

El sistema utiliza Room para la gestión de datos locales con las siguientes entidades:

- **User**: Usuarios del sistema
- **Product**: Catálogo de productos
- **Customer**: Base de datos de clientes
- **Sale**: Registro de ventas
- **SaleItem**: Items individuales de cada venta

## 🛠️ Configuración y Desarrollo

### Requisitos Previos
- Android Studio Arctic Fox o superior
- JDK 8 o superior
- Android SDK API 24 (Android 7.0) o superior
- Gradle 8.2.2

### Instalación

1. **Abrir en Android Studio**:
   - Abrir Android Studio
   - Seleccionar "Open an existing project"
   - Navegar a la carpeta del proyecto

2. **Sincronizar dependencias**:
   ```bash
   ./gradlew build
   ```

3. **Ejecutar la aplicación**:
   - Conectar un dispositivo Android o iniciar un emulador
   - Hacer clic en "Run" en Android Studio

## 📱 Uso de la Aplicación

### Primer Uso

1. **Inicio de Sesión**:
   - Usuario por defecto: `admin`
   - Contraseña por defecto: `admin123`

2. **Configuración Inicial**:
   - Configurar información del negocio
   - Agregar productos al inventario
   - Registrar clientes

### Flujo de Trabajo Típico

1. **Inicio del Día**:
   - Iniciar sesión
   - Revisar dashboard con métricas del día anterior
   - Verificar alertas de stock bajo

2. **Procesamiento de Ventas**:
   - Ir a la pantalla de Ventas
   - Buscar y agregar productos
   - Completar la venta

3. **Gestión de Inventario**:
   - Agregar nuevos productos
   - Ajustar stock según sea necesario
   - Revisar productos con stock bajo

4. **Análisis y Reportes**:
   - Revisar reportes de ventas
   - Analizar rendimiento de productos
   - Exportar datos para análisis externo

## 🔧 Configuración Avanzada

### Personalización de Tema

El tema se puede personalizar editando los archivos en `ui/theme/`:
- `Color.kt`: Colores del tema
- `Theme.kt`: Configuración de tema claro/oscuro
- `Type.kt`: Tipografía

### Configuración de Base de Datos

La base de datos se configura automáticamente en el primer inicio. Para resetear:
1. Desinstalar la aplicación
2. Reinstalar para crear una nueva base de datos

### Exportación de Datos

La aplicación incluye funcionalidad para exportar datos en formato CSV:
- Productos
- Clientes
- Ventas
- Reportes

## 🚀 Características Futuras

- [ ] Integración con impresoras térmicas
- [ ] Sincronización en la nube
- [ ] Códigos de barras y QR
- [ ] Múltiples métodos de pago
- [ ] Integración con sistemas contables
- [ ] App para tablets
- [ ] Modo offline completo
- [ ] Análisis predictivo

---

**PuntoFácil** - Simplificando la gestión de tu negocio, una venta a la vez. 🛍️

## Características Principales

### Fase 0 - Cimientos Sólidos ✅
- Arquitectura MVVM con Clean Architecture
- Base de datos SQLite con Room
- Inyección de dependencias con Hilt
- Navegación con Navigation Component
- Material Design 3
- Soporte para modo offline

### Fase 1 - MVP Definitivo 🚧
- **Punto de Venta (POS)**
  - Interfaz táctil optimizada
  - Búsqueda rápida de productos
  - Escáner de códigos de barras
  - Carrito de compras dinámico
  - Soporte para productos con variantes
  - Cálculo automático de impuestos
  - Múltiples métodos de pago

- **Gestión de Inventario**
  - CRUD completo de productos
  - Gestión de variantes (talla, color, etc.)
  - Control de stock en tiempo real
  - Alertas de stock bajo
  - Historial de movimientos

- **Gestión de Caja**
  - Apertura y cierre de caja
  - Registro de transacciones
  - Cortes de caja automáticos
  - Reportes de ventas por turno

- **Usuarios y Roles**
  - Sistema de autenticación
  - Roles y permisos
  - Gestión de empleados

### Fase 2 - Mejoras Operativas 📋
- Gestión de clientes (CRM)
- Programas de lealtad
- Devoluciones y cambios
- Órdenes de compra
- Reportes avanzados
- Modo offline mejorado

### Fase 3 - Inteligencia de Negocio 📊
- Dashboard analítico
- Reportes personalizables
- Multi-tienda
- Integración con hardware POS
- API para desarrolladores

## Tecnologías Utilizadas

### Frontend (Android)
- **Lenguaje**: Kotlin
- **UI**: Jetpack Compose + Material Design 3
- **Arquitectura**: MVVM + Clean Architecture
- **Navegación**: Navigation Compose
- **Inyección de Dependencias**: Hilt
- **Base de Datos**: Room (SQLite)
- **Networking**: Retrofit + OkHttp
- **Imágenes**: Coil
- **Cámara**: CameraX (para escáner)
- **Permisos**: Accompanist Permissions

### Backend (Opcional - para sincronización)
- **Lenguaje**: Kotlin/Java (Spring Boot) o Node.js
- **Base de Datos**: PostgreSQL/MySQL
- **API**: REST + GraphQL
- **Autenticación**: JWT
- **Documentación**: Swagger/OpenAPI

## Estructura del Proyecto

```
app/
├── src/main/java/com/puntofacil/
│   ├── data/
│   │   ├── database/
│   │   ├── repository/
│   │   └── remote/
│   ├── domain/
│   │   ├── model/
│   │   ├── repository/
│   │   └── usecase/
│   ├── presentation/
│   │   ├── ui/
│   │   │   ├── auth/
│   │   │   ├── pos/
│   │   │   ├── inventory/
│   │   │   ├── customers/
│   │   │   ├── reports/
│   │   │   └── settings/
│   │   ├── viewmodel/
│   │   └── navigation/
│   ├── di/
│   └── utils/
└── res/
    ├── layout/
    ├── values/
    └── drawable/
```

## Instalación y Configuración

### Prerrequisitos
- Android Studio Arctic Fox o superior
- JDK 11 o superior
- Android SDK API 24+ (Android 7.0)
- Dispositivo Android o emulador

### Pasos de Instalación
1. Clonar el repositorio
2. Abrir el proyecto en Android Studio
3. Sincronizar las dependencias de Gradle
4. Configurar las variables de entorno (si es necesario)
5. Ejecutar la aplicación

## Configuración de Base de Datos

La aplicación utiliza SQLite con Room para almacenamiento local. La base de datos se crea automáticamente en el primer inicio.

### Esquema Principal
- **users**: Usuarios del sistema
- **products**: Productos del inventario
- **product_variants**: Variantes de productos
- **categories**: Categorías de productos
- **customers**: Clientes
- **sales**: Ventas realizadas
- **sale_items**: Ítems de cada venta
- **inventory_movements**: Movimientos de inventario
- **cash_sessions**: Sesiones de caja
- **tax_rates**: Tasas de impuestos

## Uso de la Aplicación

### Inicio de Sesión
1. Crear cuenta de administrador en el primer uso
2. Iniciar sesión con credenciales
3. Configurar datos básicos de la empresa

### Punto de Venta
1. Abrir caja con fondo inicial
2. Buscar productos por nombre, código o escáner
3. Agregar productos al carrito
4. Aplicar descuentos si es necesario
5. Seleccionar método de pago
6. Procesar venta e imprimir recibo

### Gestión de Inventario
1. Agregar nuevos productos
2. Configurar variantes y precios
3. Actualizar stock
4. Monitorear alertas de stock bajo

## Contribución

1. Fork el proyecto
2. Crear una rama para tu feature (`git checkout -b feature/AmazingFeature`)
3. Commit tus cambios (`git commit -m 'Add some AmazingFeature'`)
4. Push a la rama (`git push origin feature/AmazingFeature`)
5. Abrir un Pull Request

## Licencia

Este proyecto está bajo la Licencia MIT - ver el archivo [LICENSE](LICENSE) para más detalles.

## Contacto

- **Proyecto**: PuntoFácil
- **Versión**: 1.0.0
- **Estado**: En Desarrollo

## Roadmap

- [x] Fase 0: Arquitectura base
- [ ] Fase 1: MVP con funcionalidades core
- [ ] Fase 2: Mejoras operativas
- [ ] Fase 3: Inteligencia de negocio
- [ ] Fase 4: Integraciones
- [ ] Fase 5: IA y funciones avanzadas