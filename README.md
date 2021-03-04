<h1 align="center">Testing con
Spring y Node.js 👨🏻‍💻 </h1>

<p align="center">
  <a href="/docs" target="_blank">
    <img alt="Documentation" src="https://img.shields.io/badge/documentation-yes-brightgreen.svg" />
  </a>
  <a href="#" target="_blank">
    <img alt="License: MIT" src="https://img.shields.io/badge/License-MIT-yellow.svg" />
  </a>
</p>

Proyecto para introducir test unitarios y de integración en un encunciado facilitado por el profesor.

## Authors
👤 **JuanCBM**: Juan Carlos Blázquez Muñoz
* Github: [@JuanCBM](https://github.com/JuanCBM)

👤 **mahuerta**: Miguel Ángel Huerta Rodríguez
* Github: [@mahuerta](https://github.com/mahuerta)


## Apuntes teóricos
- Los test rest, son test end to end (e2e), test de integración. Para que funcionen es necesario tener docker puesto que se levanta una base de datos en un contenedor.
- En los test unitarios está mockeado el acceso a la base de datos.



- MockMvc: MockMvc es una de las clases de spring-test. Esto se usa principalmente para pruebas unitarias (Test unitarios) de la capa del controlador. No solo tu clase de controlador. Esto es para probar la capa del controlador. Pero tienes que simular el servicio y otras capas. Por lo tanto, se utiliza principalmente para pruebas unitarias.

- WebTestClient: Es un cliente reactivo sin bloqueo para probar servidores web que utiliza el reactivo WebClient internamente para realizar solicitudes y proporciona una API flux para verificar las respuestas. Puede conectarse a cualquier servidor a través de HTTP, o vincularse directamente a aplicaciones WebFlux utilizando envío y respuesta simulados, sin la necesidad de un servidor HTTP.
WebTestClient es similar a MockMvc. La única diferencia entre esos clientes web de prueba es que WebTestClient tiene como objetivo probar los endpoints de WebFlux.

- RestAssured es un marco completamente diferente. Esto no tiene nada que ver con Spring. Esta es una librería, que proporciona varias formas de probar cualquier servicio REST.


## Testing
1. Pruebas unitarias un componente. mockeas dependencias al exterior
2. pruebas de integración varios componentes
3. pruebas de sistema, todas las pruebas de integracion
