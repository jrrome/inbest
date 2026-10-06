// SPDX-License-Identifier: UNLICENSED
pragma solidity ^0.8.20;

// Contrato para crear un token de prueba, guardar cuánto tiene cada
// cuenta y permitir transferencias. También establece un administrador
// que puede crear tokens nuevos.
// Los contratos, así como las cuentas de usuarios, tienen una dirección que
// permiten localizarlos

// Leer:
// https://docs.soliditylang.org/en/latest/introduction-to-smart-contracts.html
// https://docs.openzeppelin.com/contracts/5.x

// Explicación: según lo entiendo, un contrato es código que se ejecuta
// dentro de una blockchain, y entonces, puedes confiar en él (presuntamente).
// Es un sistema parecido a bitcoin, pero en vez de verificar solo transacciones
// también se verifican contratos complejos (ejecutar código)

// ERC-20 es un estándar: conjunto de funciones y comportamientos comunes
// tokens
// Aporta saldos, transferencias y más operaciones estándar de un Token
import {ERC20} from "@openzeppelin/contracts/token/ERC20/ERC20.sol";
// Aporta una cuenta admin
import {Ownable} from "@openzeppelin/contracts/access/Ownable.sol";

// Crea un contrato (SimulatedToken) que hereda de ERC20 y Ownable
contract SimulatedToken is ERC20, Ownable {
    constructor(
        // memory indica que el texto del parámetro se recibe
        // en memoria temporal durante ejecución. Aquí se usa
        // porque el constructor de ERC20 ya copia después los
        // el nombre y símbolo al almacenamiento persistente del contrato

        // Nombre del token (por ejemplo, ETSECoin)
        string memory tokenName,
        // Símbolo del token (ETSEC)
        string memory tokenSymbol,
        // Cuenta del admin inicial
        address initialOwner
    )
        // Llamadas a los constructores de los contratos heredados
        ERC20(tokenName, tokenSymbol)
        Ownable(initialOwner)
    {}

    /// Función para emitir tokens
    // mint, en el contexto de tokens, significa emitir o crear unidades
    // nuevas
    /// @param recipient es la dirección de la cuenta que va a recibir
    /// los tokens
    /// @param amount es la cantidad del token que se va a emitir, expresada
    /// como un entero sin signo de 256 bits
    // external permite llamar a la función desde fuera del contrato, desde
    // una cuenta u otro contrato
    // onlyOwner es un modificador que exige que quien llama sea el admin actual
    function mint(address recipient, uint256 amount) external onlyOwner {
        // Función implementada en ERC20:
        // 1. Aumenta el saldo del destinatario
        // 2. Aumenta la cantidad total de tokens existentes
        // 3. Emite un evento Transfer que registra la creación
        _mint(recipient, amount);
        // amount se mide en unidades mínimas del token, este ERC-20, por ejemplo,
        // tiene 18 decimales, así que cada unidad representa 1/10^18 tokens
        // 1 token = 10^18 unidades
    }

}
