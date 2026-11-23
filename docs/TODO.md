# TO DO

- Añadir saldo (cash)
- Comprar X cantidad de un token
- Crear usuario sistema
- Al comprar un token, asignarle 10k del mismo al sistema, crear órdenes
- Vender X cantidad de un token


Crear token: 
Añadido: 
Se mintea X cantidad de un token y se añaden a la wallet del sistema.
Después, el sistema debe crear órdenes de compra y venta
Todo esto debe ser una tarea asíncrona

Ejemplo de crear token:
[todo lo que ya hay ahora]
llamada a método asíncrono:
    -> meter en cola de eventos:
        -> mintear x cantidad del token a la wallet del sistema
        -> crear ordenes de compra y venta

Preguntas, la wallet del sistema, de momento solo tener 1?
crearla manualmente con sql directamente?
hay que conocerla, se pone en application.properties?

