// SPDX-License-Identifier: UNLICENSED
pragma solidity ^0.8.20;

// Contrato base de forge para tests
import {Test} from "forge-std/Test.sol";
import {Ownable} from "@openzeppelin/contracts/access/Ownable.sol";
// Aporta errores ERC20
import {IERC20Errors} from "@openzeppelin/contracts/interfaces/draft-IERC6093.sol";
import {SimulatedToken} from "../src/SimulatedToken.sol";

// https://www.getfoundry.sh/forge/testing

// Tests básicos que comprueban los fundamentos de los tokens que siguen
// el contrato SimulatedToken
contract SimulatedTokenTest is Test {
    uint256 private constant UNIT = 10 ** 18;

    SimulatedToken private token;

    address private admin;
    // Cuentas de ejemplo
    address private jaime;
    address private miguel;

    // Prepara el estado inicial de cada prueba
    function setUp() public {
        // makeAddr obtiene una dirección de prueba y le asigna una etiqueta
        // para ser reconocida
        admin = makeAddr("admin");
        jaime = makeAddr("jaime");
        miguel = makeAddr("miguel");

        token = new SimulatedToken("Best Coin", "BSTC", admin);
    }

    function test_MintIncreasesBalanceAndSupply() public {
        // Indica que el remitente de la siguiente operación será admin
        vm.prank(admin);
        // Emitir 100 tokens asignados a jaime
        token.mint(jaime, 100 * UNIT);

        // Tanto el balance de jaime como el supply total deben ser 100 BSTC
        assertEq(token.balanceOf(jaime), 100 * UNIT);
        assertEq(token.totalSupply(), 100 * UNIT);
    }

    function test_OnlyOwnerCanMint() public {
        // Con expectRever avisamos a forge de que esperamos un fallo
        // El error esperado es Ownable.OwnableUnauthorizedAccount
        // .selector es el identificador del error
        // La prueba pasa si jaime consigue emitir o si se recibe un error
        // diferente
        vm.expectRevert(
            abi.encodeWithSelector(
                Ownable.OwnableUnauthorizedAccount.selector,
                jaime
            )
        );

        vm.prank(jaime);
        token.mint(jaime, 100 * UNIT);

        // No se debería haber emitido nada y el supply total y el saldo
        // de jaime deberían ser 0
        assertEq(token.balanceOf(jaime), 0);
        assertEq(token.totalSupply(), 0);
    }

    function test_TransferUpdatesBalancesAndPreservesSupply() public {
        vm.prank(admin);
        token.mint(jaime, 100 * UNIT);

        // Jaime transfiere 25 BSTC a Miguel
        vm.prank(jaime);
        token.transfer(miguel, 25 * UNIT);

        assertEq(token.balanceOf(jaime), 75 * UNIT);
        assertEq(token.balanceOf(miguel), 25 * UNIT);
        assertEq(token.totalSupply(), 100 * UNIT);
    }

    function test_CannotTransferMoreThanBalance() public {
        vm.prank(admin);
        token.mint(jaime, 100 * UNIT);

        vm.expectRevert(
            abi.encodeWithSelector(
                IERC20Errors.ERC20InsufficientBalance.selector,
                jaime,
                100 * UNIT,
                200 * UNIT
            )
        );

        vm.prank(jaime);
        token.transfer(miguel, 200 * UNIT);

        assertEq(token.balanceOf(jaime), 100 * UNIT);
        assertEq(token.balanceOf(miguel), 0);
        assertEq(token.totalSupply(), 100 * UNIT);
    }
}
