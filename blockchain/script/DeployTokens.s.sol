// SPDX-License-Identifier: UNLICENSED
pragma solidity ^0.8.20;

// Contrato base de Foundry
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

        vm.startBroadcast(privateKey);

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
