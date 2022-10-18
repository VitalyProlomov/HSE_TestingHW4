import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestVendingMachine {
    private final int MAX1 = 30;
    private final int MAX2 = 40;

    private final int MAX_C = 50;

    private final long CODE = 117345294655382L;

    private final int VALUE1 = 1;
    private final int VALUE2 = 2;

    private final int PRICE1 = 8;
    private final int PRICE2 = 5;

    private static VendingMachine vm;

    @BeforeEach
    public void initiateVendingMachine() {
        vm = new VendingMachine();
    }

    @Test
    public void testAdminMode() {
        assertEquals(VendingMachine.Mode.OPERATION, vm.getCurrentMode());

        assertEquals(VendingMachine.Response.OK, vm.enterAdminMode(CODE));
        assertEquals(VendingMachine.Mode.ADMINISTERING, vm.getCurrentMode());

        assertEquals(VendingMachine.Response.INVALID_PARAM, vm.enterAdminMode(42));
        assertEquals(VendingMachine.Mode.ADMINISTERING, vm.getCurrentMode());

        vm.exitAdminMode();
        assertEquals(VendingMachine.Mode.OPERATION, vm.getCurrentMode());
    }

    @Test
    public void testGetPrice() {
        assertEquals(PRICE1, vm.getPrice1());
        assertEquals(PRICE2, vm.getPrice2());
    }

    @Test
    public void testGetCoins() {
        // Operation Mode
        assertEquals(0, vm.getCoins1());
        assertEquals(0, vm.getCoins2());

        assertEquals(VendingMachine.Response.OK, vm.enterAdminMode(CODE));
        // Admin Mode
        assertEquals(0, vm.getCoins1());
        assertEquals(0, vm.getCoins2());
    }

    @Test
    public void testFillCoins() {
        assertEquals(VendingMachine.Response.ILLEGAL_OPERATION, vm.fillCoins(1, 1));

        vm.enterAdminMode(CODE);
        vm.fillCoins(10, 11);
        assertEquals(10, vm.getCoins1());
        assertEquals(11, vm.getCoins2());

        vm.fillCoins(1, 1);
        assertEquals(1, vm.getCoins1());
        assertEquals(1, vm.getCoins2());

        assertEquals(VendingMachine.Response.INVALID_PARAM, vm.fillCoins(-1, -1));
        assertEquals(1, vm.getCoins1());
        assertEquals(1, vm.getCoins2());

        // Found mistake by this test
        assertEquals(VendingMachine.Response.INVALID_PARAM, vm.fillCoins(60, 5));
        assertEquals(1, vm.getCoins1());
        assertEquals(1, vm.getCoins2());

        assertEquals(VendingMachine.Response.INVALID_PARAM, vm.fillCoins(5, 60));
        assertEquals(1, vm.getCoins1());
        assertEquals(1, vm.getCoins2());

        assertEquals(VendingMachine.Response.INVALID_PARAM, vm.fillCoins(60, 60));
        assertEquals(1, vm.getCoins1());
        assertEquals(1, vm.getCoins2());

        assertEquals(VendingMachine.Response.INVALID_PARAM, vm.fillCoins(-1, 60));
        assertEquals(1, vm.getCoins1());
        assertEquals(1, vm.getCoins2());

        assertEquals(VendingMachine.Response.INVALID_PARAM, vm.fillCoins(5, -1));
        assertEquals(1, vm.getCoins1());
        assertEquals(1, vm.getCoins2());


        assertEquals(VendingMachine.Response.OK, vm.fillCoins(15, 5));
        assertEquals(15, vm.getCoins1());
        assertEquals(5, vm.getCoins2());

    }

    @Test
    public void testPutCoinsErrorCases() {
        // Adding coins in OPERATION Mode - must be OK
        assertEquals(VendingMachine.Response.OK, vm.putCoin1());
        assertEquals(VendingMachine.Response.OK, vm.putCoin2());

        // Must return 0, since we are in the Operation Mode
        assertEquals(0, vm.getCoins1());
        assertEquals(0, vm.getCoins2());

        // Cannot enter Operation Mode when balance is not 0.
        assertEquals(VendingMachine.Response.CANNOT_PERFORM, vm.enterAdminMode(CODE));
        assertEquals(vm.getCurrentMode(), VendingMachine.Mode.OPERATION);

        vm = new VendingMachine();
        vm.enterAdminMode(CODE);
        //  Can not add coins into the machine while running an administering mode.
        assertEquals(VendingMachine.Response.ILLEGAL_OPERATION, vm.putCoin1());
        assertEquals(VendingMachine.Response.ILLEGAL_OPERATION, vm.putCoin2());

        // Coins should not have been added.
        assertEquals(0, vm.getCoins1());
        assertEquals(0, vm.getCoins2());

        vm.exitAdminMode();
        // Putting coins up to the max amount
        for (int i = 0; i < MAX_C; ++i) {
            assertEquals(VendingMachine.Response.OK, vm.putCoin1());
        }

        // Maximum coins reached
        assertEquals(VendingMachine.Response.CANNOT_PERFORM, vm.putCoin1());

        for (int i = 0; i < MAX_C; ++i) {
            assertEquals(VendingMachine.Response.OK, vm.putCoin2());
        }
        assertEquals(VendingMachine.Response.CANNOT_PERFORM, vm.putCoin2());
    }

    @Test
    public void testPutCoinsBalance() {
        vm.putCoin1();
        assertEquals(VALUE1, vm.getCurrentBalance());

        vm = new VendingMachine();
        vm.putCoin2();
        assertEquals(VALUE2, vm.getCurrentBalance());
        vm.putCoin1();
        vm.putCoin1();
        assertEquals(VALUE2 + VALUE1 * 2, vm.getCurrentBalance());
    }


    // Ideally we can also check it in the reverse order (first fill up the 2nd type of coins, then
    // the 1st one) - it can actually change the outcome if for example putCoin1() is mistakenly
    // using the info of coins2. But that will take too much place, and that is not the case here.

    // We can not properly test putting coins, because the task is formulated in such a way that
    // the methods getCoins1() and getCoins2() must always return 0.
    // A more elaborate explanation  is given in the Word file attached to the homework (after mistake №4)


    @Test
    public void testFillProducts() {
        assertEquals(VendingMachine.Mode.OPERATION, vm.getCurrentMode());
        assertEquals(0, vm.getNumberOfProduct1());
        assertEquals(0, vm.getNumberOfProduct2());

        vm.fillProducts();
        assertEquals(0, vm.getNumberOfProduct1());
        assertEquals(0, vm.getNumberOfProduct2());

        vm.enterAdminMode(CODE);
        vm.fillProducts();
        assertEquals(MAX1, vm.getNumberOfProduct1());
        assertEquals(MAX2, vm.getNumberOfProduct2());

        vm.exitAdminMode();
    }


    @Test
    public void testReturnMoneyWithoutExtraMoney() {
        // vm.enterAdminMode(CODE);
        for (int i = 0; i < 3; ++i) {
            vm.putCoin1();
        }
        assertEquals(3 * VALUE1, vm.getCurrentBalance());
        assertEquals(VendingMachine.Response.OK, vm.returnMoney());
        assertEquals(0, vm.getCurrentBalance());

        for (int i = 0; i < MAX_C; ++i) {
            vm.putCoin2();
        }
        assertEquals(MAX_C * VALUE2, vm.getCurrentBalance());
        assertEquals(VendingMachine.Response.OK, vm.returnMoney());
        assertEquals(0, vm.getCurrentBalance());

        vm.putCoin1();
        vm.putCoin2();
        assertEquals(VendingMachine.Response.OK, vm.returnMoney());
        assertEquals(0, vm.getCurrentBalance());

        assertEquals(VendingMachine.Response.OK, vm.returnMoney());

        vm.enterAdminMode(CODE);
        assertEquals(VendingMachine.Response.ILLEGAL_OPERATION, vm.returnMoney());
    }

    @Test
    public void testReturnMoneyWithExtraMoney() {
        vm.enterAdminMode(CODE);
        vm.fillCoins(15, 15);
        vm.exitAdminMode();
        vm.putCoin2();
        vm.putCoin2();

        assertEquals(4, vm.getCurrentBalance());
        assertEquals(VendingMachine.Response.OK, vm.returnMoney());

        vm.enterAdminMode(CODE);
        assertEquals(15, vm.getCoins1());
        assertEquals(15, vm.getCoins2());
        assertEquals(15 * VALUE1 + 15 * VALUE2, vm.getCurrentSum());
    }

    private void fillMachine(VendingMachine vm) {
        vm.enterAdminMode(CODE);
        vm.fillCoins(15, 10);
        vm.exitAdminMode();
    }
    @Test
    public void testGiveProducts() {
        assertEquals(VendingMachine.Response.INSUFFICIENT_PRODUCT, vm.giveProduct1(1));
        vm.enterAdminMode(CODE);
        vm.fillProducts();
        assertEquals(VendingMachine.Response.ILLEGAL_OPERATION, vm.giveProduct1(1));
        vm.exitAdminMode();
        assertEquals(VendingMachine.Response.INSUFFICIENT_MONEY, vm.giveProduct1(1));
        for (int i = 0; i < 10; ++i) {
            vm.putCoin2();
        }
        assertEquals(VALUE2 * 10 - PRICE1, vm.getCurrentBalance());
        assertEquals(VendingMachine.Response.OK, vm.giveProduct1(1));
        assertEquals(VALUE2 * 10 - PRICE1, vm.getCurrentBalance());

    }
}
