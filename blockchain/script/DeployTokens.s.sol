// SPDX-License-Identifier: UNLICENSED
pragma solidity ^0.8.20;

// Contrato base de Foundry para scripts
import {Script} from "forge-std/Script.sol";
// Herramienta para mostrar mensajes
import {console2} from "forge-std/console2.sol";
// Nuestro contrato
import {SimulatedToken} from "../src/SimulatedToken.sol";

contract DeployTokens is Script {
    function run() external {
        // vm lo aporta Script, que ofrece herramientas de Forge
        uint256 privateKey = vm.envUint("PRIVATE_KEY");
        address admin = vm.addr(privateKey);

        // Indica a Forge que las siguientes operaciones las prepare como
        // transacciones realizadas con la cuenta privateKey (firmadas)
        // Para que estas transacciones se firmen y se envíen a anvil (no solo
        // se simulen) hay que ejecutar el script con --broadcast
        vm.startBroadcast(privateKey);

        // Creación de un token "Best Coin" con código BSTC
        SimulatedToken bestcoin = new SimulatedToken(
            "Best Coin",
            "BSTC",
            admin
        );

        SimulatedToken euro = new SimulatedToken(
            "Simulated Euro",
            "SEUR",
            admin
        );

        // Finaliza el tramo de recopilación de transacciones
        vm.stopBroadcast();

        console2.log("SEUR:", address(euro));
        console2.log("BSTC:", address(bestcoin));
    }
}
