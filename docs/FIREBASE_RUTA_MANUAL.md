# Ruta manual y preparacion para notificaciones

## Alcance de esta etapa

La app del chofer ya puede:

1. Abrir `Armar Ruta` desde la pantalla `Buscar`.
2. Tomar los clientes del camion elegido que corresponden al dia actual.
3. Seleccionar y ordenar manualmente las paradas.
4. Crear en Cloud Firestore una ruta con estado `started`.

La notificacion push no se envia todavia. Para enviarla faltan la app/perfil
del cliente, el registro de tokens FCM y una Cloud Function.

## Configuracion necesaria en Firebase Console

### 1. Cloud Firestore

En Firebase Console abre `Firestore Database`. La coleccion `routes` no se
crea manualmente: aparece al iniciar la primera ruta desde la app.

El documento que crea la app tiene esta forma:

```text
routes/{fecha}_{camion}_{driverUid}
  driverId: string
  truckId: number
  date: "YYYY-MM-DD"
  status: "started"
  createdAt: timestamp
  startedAt: timestamp
  notificationDispatched: false
  stops: [
    {
      clientId: string,
      clientName: string,
      address: string,
      order: number,
      status: "pending"
    }
  ]
```

Para esta prueba inicial, confirma que un usuario autenticado pueda crear un
documento en `routes`. Para produccion, configura roles antes de restringir la
escritura a choferes.

### 2. Perfiles de usuario

Crea una coleccion `users`. Cada usuario autenticado debe tener un documento
con el mismo id que su `uid` de Firebase Authentication:

```text
users/{uid}
  email: "chofer@ejemplo.com"
  role: "driver"
```

Para clientes:

```text
users/{uid}
  email: "cliente@ejemplo.com"
  role: "client"
  clientId: "id_del_documento_en_clientes"
```

Tambien se debe agregar `userId` al documento de `clientes` que corresponda a
ese usuario cliente. Ese vinculo indica quien recibira la notificacion de una
parada.

### 3. Firebase Cloud Messaging

En Firebase Console, Cloud Messaging ya usa el mismo proyecto asociado a
`app/google-services.json`. La app cliente debera:

1. Agregar la dependencia `firebase-messaging`.
2. Pedir permiso de notificaciones en Android 13 o superior.
3. Obtener el token FCM del dispositivo.
4. Guardarlo en Firestore:

```text
users/{uid}/devices/{tokenId}
  token: string
  platform: "android"
  updatedAt: timestamp
```

### 4. Cloud Functions

Las notificaciones deben enviarse desde Cloud Functions, no desde la app
Android. La funcion futura observara:

```text
routes/{routeId}
```

Al detectar una ruta nueva con `status == "started"` y
`notificationDispatched == false`, debe:

1. Recorrer `stops`.
2. Consultar el `userId` asociado a cada `clientId`.
3. Leer los tokens de `users/{uid}/devices`.
4. Enviar el mensaje con Firebase Admin SDK.
5. Actualizar `notificationDispatched` a `true`.

La funcion debe ser idempotente, porque los eventos de Firestore pueden
entregarse mas de una vez.

## Reglas sugeridas para la etapa con roles

Estas reglas son una base para revisar antes de publicarlas. Solo habilites la
restriccion de `routes` despues de haber creado los documentos `users` de tus
choferes.

```javascript
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    function signedIn() {
      return request.auth != null;
    }

    function isDriver() {
      return signedIn()
        && get(/databases/$(database)/documents/users/$(request.auth.uid)).data.role == "driver";
    }

    match /routes/{routeId} {
      allow create: if isDriver()
        && request.resource.data.driverId == request.auth.uid
        && request.resource.data.status == "started";
      allow read: if signedIn();
      allow update: if isDriver()
        && resource.data.driverId == request.auth.uid;
    }
  }
}
```

## Siguiente implementacion

La siguiente entrega debe incorporar roles reales y FCM en Android. Una vez
que un cliente pueda iniciar sesion y su token quede en `users/{uid}/devices`,
la Cloud Function podra mandar la notificacion cuando el chofer pulse
`Iniciar Ruta`.
