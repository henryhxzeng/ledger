package com.ledger.cqrs.aggregate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import com.ledger.common.Status;
import com.ledger.cqrs.command.FundInCommand;
import com.ledger.cqrs.command.FundOutCommand;
import com.ledger.cqrs.command.MoveAssetCommand;
import com.ledger.cqrs.command.OpenAccountCommand;
import com.ledger.cqrs.command.UpdateAccountStatusCommand;
import com.ledger.cqrs.repository.AccountReadRepository;
import com.ledger.domain.Account;
import com.ledger.domain.Wallet;
import com.ledger.dto.AccountInfo;
import com.ledger.dto.FundIn;
import com.ledger.dto.FundOut;
import com.ledger.dto.MoveAsset;
import com.ledger.dto.WalletInfo;
import com.ledger.eventsourcing.repository.EventStore;

@ExtendWith(MockitoExtension.class)
public class AccountAggregateTest {

	@Mock
    private EventStore eventRepository;
	@Mock
    private AccountReadRepository readRepository;
	
	@InjectMocks
	private AccountAggregate accountAggregate;
	
	private static final String ENTITY_UUID_STRING = "a8c3ae09-d2a6-4b23-9218-15b54fdd69bf";
	private static final String ENTITY_UUID_STRING2 = "c2b3c2a8-62c0-4f63-94ad-49dd360ccbf2";
	private static final String ACCOUNT_UUID_STRING = "3c5e645e-b36a-4454-a123-06a081cc5b70";
	private static final String ACCOUNT_UUID_STRING2 = "8a494c36-df3f-42c0-a120-bf3706c1a7ff";
	private static final String WALLET_UUID_STRING = "7a5aebcb-d832-4cee-b306-375e37064f98";
	private static final String WALLET_UUID_STRING2 = "3b64e227-8d80-42cc-866b-afce07e4705c";
	private static final String CLOSED_ACCOUNT_UUID_STRING = "3b64e227-8d80-42cc-866b-afce07e4705c";

	@Test
	public void test_HandleOpenAccountCommand() {
		WalletInfo walletInfo = WalletInfo.builder().type("BOND").walletName("Wallet Name").build();
		List<WalletInfo> walletInfos = new ArrayList<WalletInfo>();
		walletInfos.add(walletInfo);
		AccountInfo accountInfo = AccountInfo.builder().accountName("Account Name").wallets(walletInfos).build();
		List<AccountInfo> accountInfos = new ArrayList<AccountInfo>();
		accountInfos.add(accountInfo);
		OpenAccountCommand command = OpenAccountCommand.builder().entityId(ENTITY_UUID_STRING).entityName("Name").account(accountInfos).build();
		accountAggregate.handleOpenAccountCommand(command);
		// verify event is added into eventRepository
		verify(eventRepository, times(1)).addEvent(Mockito.anyString(), Mockito.any());
	}
	
	
	@Test
	public void test_HandleUpdateAccountStatusCommand_CheckStatus() {
		
		Account account = Account.builder().accountId(CLOSED_ACCOUNT_UUID_STRING).status(Status.CLOSED).build();
		when(readRepository.getAccount(Mockito.anyString())).thenReturn(account);
		
		UpdateAccountStatusCommand command = new UpdateAccountStatusCommand("", "CLOSED");
		
		IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
			accountAggregate.handleUpdateAccountStatusCommand(command);
		});
		assertEquals(exception.getMessage(), "Update account status declined, need to update status from one to another");
	}
	
	@Test
	public void test_HandleUpdateAccountStatusCommand() {
		Account account = Account.builder().entityId(ENTITY_UUID_STRING).status(Status.OPEN).build();
		when(readRepository.getAccount(Mockito.anyString())).thenReturn(account);
		
		UpdateAccountStatusCommand command = new UpdateAccountStatusCommand(ACCOUNT_UUID_STRING, "CLOSED");
		accountAggregate.handleUpdateAccountStatusCommand(command);
		// verify event is added into eventRepository
		verify(eventRepository, times(1)).addEvent(Mockito.anyString(), Mockito.any());
	}
	
	@Test
	public void test_HandleFundInCommand_CheckAccountStatus() {
		FundIn fundIn = FundIn.builder().walletId(WALLET_UUID_STRING).amount(10.0).build();
		List<FundIn> requests = new ArrayList<>();
		requests.add(fundIn);
		FundInCommand command = FundInCommand.builder().requests(requests).build();
		Account account = Account.builder().accountId(CLOSED_ACCOUNT_UUID_STRING).status(Status.CLOSED).build();
		when(readRepository.getAccountIdByWalletId(Mockito.anyString())).thenReturn(CLOSED_ACCOUNT_UUID_STRING);
		when(readRepository.getEntityIdByAccountId(Mockito.anyString())).thenReturn(ENTITY_UUID_STRING);
		when(readRepository.getAccount(Mockito.anyString())).thenReturn(account);
		
		IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
			accountAggregate.handleFundInCommand(command);
		});
		assertEquals(exception.getMessage(), 
				MessageFormat.format("The account {0} is CLOSED, we cannot do movement for wallet {1} in CLOSED account", CLOSED_ACCOUNT_UUID_STRING, WALLET_UUID_STRING));
	}
	
	@Test
	public void test_HandleFundInCommand_CheckEntity() {
		FundIn fundIn = FundIn.builder().walletId(WALLET_UUID_STRING).amount(10.0).build();
		FundIn fundIn2 = FundIn.builder().walletId(WALLET_UUID_STRING2).amount(10.0).build();
		List<FundIn> requests = new ArrayList<>();
		requests.add(fundIn);
		requests.add(fundIn2);
		FundInCommand command = FundInCommand.builder().requests(requests).build();
		Account account = Account.builder().accountId(ACCOUNT_UUID_STRING).status(Status.OPEN).build();
		when(readRepository.getAccountIdByWalletId(WALLET_UUID_STRING)).thenReturn(ACCOUNT_UUID_STRING);
		when(readRepository.getEntityIdByAccountId(ACCOUNT_UUID_STRING)).thenReturn(ENTITY_UUID_STRING);
		when(readRepository.getAccountIdByWalletId(WALLET_UUID_STRING2)).thenReturn(ACCOUNT_UUID_STRING2);
		when(readRepository.getEntityIdByAccountId(ACCOUNT_UUID_STRING2)).thenReturn(ENTITY_UUID_STRING2);
		
		when(readRepository.getAccount(Mockito.anyString())).thenReturn(account);
		
		IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
			accountAggregate.handleFundInCommand(command);
		});
		assertEquals(exception.getMessage(), "All movements shall be in the same entity");
	}
	
	@Test
	public void test_HandleFundInCommand() {
		FundIn fundIn = FundIn.builder().walletId(WALLET_UUID_STRING).amount(10.0).build();
		List<FundIn> requests = new ArrayList<>();
		requests.add(fundIn);
		FundInCommand command = FundInCommand.builder().requests(requests).build();
		Account account = Account.builder().accountId(ACCOUNT_UUID_STRING).status(Status.OPEN).build();
		when(readRepository.getAccountIdByWalletId(WALLET_UUID_STRING)).thenReturn(ACCOUNT_UUID_STRING);
		when(readRepository.getEntityIdByAccountId(ACCOUNT_UUID_STRING)).thenReturn(ENTITY_UUID_STRING);
		when(readRepository.getEntityIdByWalletId(WALLET_UUID_STRING)).thenReturn(ENTITY_UUID_STRING);
		when(readRepository.getAccount(Mockito.anyString())).thenReturn(account);
		accountAggregate.handleFundInCommand(command);
		// verify event is added into eventRepository
		verify(eventRepository, times(1)).addEvent(Mockito.anyString(), Mockito.any());
	}
	
	@Test
	public void test_HandleFundOutCommand_CheckAccountStatus() {
		FundOut fundOut = FundOut.builder().walletId(WALLET_UUID_STRING).amount(10.0).build();
		List<FundOut> requests = new ArrayList<>();
		requests.add(fundOut);
		FundOutCommand command = FundOutCommand.builder().requests(requests).build();
		Account account = Account.builder().accountId(CLOSED_ACCOUNT_UUID_STRING).status(Status.CLOSED).build();
		when(readRepository.getAccountIdByWalletId(Mockito.anyString())).thenReturn(CLOSED_ACCOUNT_UUID_STRING);
		when(readRepository.getEntityIdByAccountId(Mockito.anyString())).thenReturn(ENTITY_UUID_STRING);
		when(readRepository.getAccount(Mockito.anyString())).thenReturn(account);
		
		IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
			accountAggregate.handleFundOutCommand(command);
		});
		assertEquals(exception.getMessage(), 
				MessageFormat.format("The account {0} is CLOSED, we cannot do movement for wallet {1} in CLOSED account", CLOSED_ACCOUNT_UUID_STRING, WALLET_UUID_STRING));
	}
	
	@Test
	public void test_HandleFundOutCommand_CheckEntity() {
		FundOut fundOut = FundOut.builder().walletId(WALLET_UUID_STRING).amount(10.0).build();
		FundOut fundOut2 = FundOut.builder().walletId(WALLET_UUID_STRING2).amount(10.0).build();
		List<FundOut> requests = new ArrayList<>();
		requests.add(fundOut);
		requests.add(fundOut2);
		FundOutCommand command = FundOutCommand.builder().requests(requests).build();
		Account account = Account.builder().accountId(ACCOUNT_UUID_STRING).status(Status.OPEN).build();
		when(readRepository.getAccountIdByWalletId(WALLET_UUID_STRING)).thenReturn(ACCOUNT_UUID_STRING);
		when(readRepository.getEntityIdByAccountId(ACCOUNT_UUID_STRING)).thenReturn(ENTITY_UUID_STRING);
		when(readRepository.getAccountIdByWalletId(WALLET_UUID_STRING2)).thenReturn(ACCOUNT_UUID_STRING2);
		when(readRepository.getEntityIdByAccountId(ACCOUNT_UUID_STRING2)).thenReturn(ENTITY_UUID_STRING2);
		
		when(readRepository.getAccount(Mockito.anyString())).thenReturn(account);
		
		IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
			accountAggregate.handleFundOutCommand(command);
		});
		assertEquals(exception.getMessage(), "All movements shall be in the same entity");
	}
	
	@Test
	public void test_HandleFundOutCommand_InsufficientBalance() {
		FundOut fundOut = FundOut.builder().walletId(WALLET_UUID_STRING).amount(10.0).build();
		List<FundOut> requests = new ArrayList<>();
		requests.add(fundOut);
		FundOutCommand command = FundOutCommand.builder().requests(requests).build();
		Account account = Account.builder().accountId(ACCOUNT_UUID_STRING).status(Status.OPEN).build();
		Wallet wallet = Wallet.builder().balance(5.0).build();
		when(readRepository.getAccountIdByWalletId(WALLET_UUID_STRING)).thenReturn(ACCOUNT_UUID_STRING);
		when(readRepository.getEntityIdByAccountId(ACCOUNT_UUID_STRING)).thenReturn(ENTITY_UUID_STRING);
		when(readRepository.getAccount(ACCOUNT_UUID_STRING)).thenReturn(account);
		// the wallet balance is 5.0, while the client request fund out 10.0
		when(readRepository.getWallet(WALLET_UUID_STRING)).thenReturn(wallet);
		IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
			accountAggregate.handleFundOutCommand(command);
		});
		assertEquals(exception.getMessage(), 
				MessageFormat.format("Insufficient balance for wallet id {0}", WALLET_UUID_STRING));
		
	}
	
	@Test
	public void test_HandleFundOutCommand() {
		FundOut fundOut = FundOut.builder().walletId(WALLET_UUID_STRING).amount(1.0).build();
		List<FundOut> requests = new ArrayList<>();
		requests.add(fundOut);
		FundOutCommand command = FundOutCommand.builder().requests(requests).build();
		Account account = Account.builder().accountId(ACCOUNT_UUID_STRING).status(Status.OPEN).build();
		Wallet wallet = Wallet.builder().balance(5.0).build();
		when(readRepository.getAccountIdByWalletId(WALLET_UUID_STRING)).thenReturn(ACCOUNT_UUID_STRING);
		when(readRepository.getEntityIdByAccountId(ACCOUNT_UUID_STRING)).thenReturn(ENTITY_UUID_STRING);
		when(readRepository.getEntityIdByWalletId(WALLET_UUID_STRING)).thenReturn(ENTITY_UUID_STRING);
		when(readRepository.getAccount(ACCOUNT_UUID_STRING)).thenReturn(account);
		when(readRepository.getWallet(WALLET_UUID_STRING)).thenReturn(wallet);
		accountAggregate.handleFundOutCommand(command);
		// verify event is added into eventRepository
		verify(eventRepository, times(1)).addEvent(Mockito.anyString(), Mockito.any());
	}
	
	@Test
	public void test_HandleMoveAssetCommand_CheckAccountStatus() {
		MoveAsset moveAsset = MoveAsset.builder().fromWalletId(WALLET_UUID_STRING).toWalletId(WALLET_UUID_STRING2).amount(10.0).build();
		List<MoveAsset> requests = new ArrayList<>();
		requests.add(moveAsset);
		MoveAssetCommand command = MoveAssetCommand.builder().requests(requests).build();
		Account account = Account.builder().accountId(CLOSED_ACCOUNT_UUID_STRING).status(Status.CLOSED).build();
		when(readRepository.getAccountIdByWalletId(Mockito.anyString())).thenReturn(CLOSED_ACCOUNT_UUID_STRING);
		when(readRepository.getEntityIdByAccountId(Mockito.anyString())).thenReturn(ENTITY_UUID_STRING);
		when(readRepository.getAccount(Mockito.anyString())).thenReturn(account);
		
		IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
			accountAggregate.handleMoveAssetCommand(command);
		});
		assertEquals(exception.getMessage(), 
				MessageFormat.format("The account {0} is CLOSED, we cannot do movement for wallet {1} in CLOSED account", CLOSED_ACCOUNT_UUID_STRING, WALLET_UUID_STRING));
	}
	
	@Test
	public void test_HandleMoveAssetCommand_CheckEntity() {
		MoveAsset moveAsset = MoveAsset.builder().fromWalletId(WALLET_UUID_STRING).toWalletId(WALLET_UUID_STRING2).amount(10.0).build();
		List<MoveAsset> requests = new ArrayList<>();
		requests.add(moveAsset);
		MoveAssetCommand command = MoveAssetCommand.builder().requests(requests).build();
		Account account = Account.builder().accountId(ACCOUNT_UUID_STRING).status(Status.OPEN).build();
		when(readRepository.getAccountIdByWalletId(WALLET_UUID_STRING)).thenReturn(ACCOUNT_UUID_STRING);
		when(readRepository.getEntityIdByAccountId(ACCOUNT_UUID_STRING)).thenReturn(ENTITY_UUID_STRING);
		when(readRepository.getAccountIdByWalletId(WALLET_UUID_STRING2)).thenReturn(ACCOUNT_UUID_STRING2);
		when(readRepository.getEntityIdByAccountId(ACCOUNT_UUID_STRING2)).thenReturn(ENTITY_UUID_STRING2);
		
		when(readRepository.getAccount(Mockito.anyString())).thenReturn(account);
		
		IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
			accountAggregate.handleMoveAssetCommand(command);
		});
		assertEquals(exception.getMessage(), "All movements shall be in the same entity");
	}
	
	@Test
	public void test_HandleMoveAssetCommand_InsufficientBalance() {
		MoveAsset moveAsset = MoveAsset.builder().fromWalletId(WALLET_UUID_STRING).toWalletId(WALLET_UUID_STRING2).amount(10.0).build();
		List<MoveAsset> requests = new ArrayList<>();
		requests.add(moveAsset);
		MoveAssetCommand command = MoveAssetCommand.builder().requests(requests).build();
		Account account = Account.builder().accountId(ACCOUNT_UUID_STRING).status(Status.OPEN).build();
		Wallet wallet = Wallet.builder().balance(5.0).build();
		when(readRepository.getAccountIdByWalletId(WALLET_UUID_STRING)).thenReturn(ACCOUNT_UUID_STRING);
		when(readRepository.getEntityIdByAccountId(ACCOUNT_UUID_STRING)).thenReturn(ENTITY_UUID_STRING);
		when(readRepository.getAccount(ACCOUNT_UUID_STRING)).thenReturn(account);
		when(readRepository.getAccountIdByWalletId(WALLET_UUID_STRING2)).thenReturn(ACCOUNT_UUID_STRING2);
		when(readRepository.getEntityIdByAccountId(ACCOUNT_UUID_STRING2)).thenReturn(ENTITY_UUID_STRING);
		when(readRepository.getAccount(ACCOUNT_UUID_STRING2)).thenReturn(account);
		// the wallet balance is 5.0, while the client request fund out 10.0
		when(readRepository.getWallet(Mockito.anyString())).thenReturn(wallet);
		IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
			accountAggregate.handleMoveAssetCommand(command);
		});
		assertEquals(exception.getMessage(), 
				MessageFormat.format("Insufficient balance for wallet id {0}", WALLET_UUID_STRING));
	}
	
	@Test
	public void test_HandleMoveAssetCommand() {
		MoveAsset moveAsset = MoveAsset.builder().fromWalletId(WALLET_UUID_STRING).toWalletId(WALLET_UUID_STRING2).amount(1.0).build();
		List<MoveAsset> requests = new ArrayList<>();
		requests.add(moveAsset);
		MoveAssetCommand command = MoveAssetCommand.builder().requests(requests).build();
		Account account = Account.builder().accountId(ACCOUNT_UUID_STRING).status(Status.OPEN).build();
		Wallet wallet = Wallet.builder().balance(5.0).build();
		when(readRepository.getAccountIdByWalletId(WALLET_UUID_STRING)).thenReturn(ACCOUNT_UUID_STRING);
		when(readRepository.getEntityIdByAccountId(ACCOUNT_UUID_STRING)).thenReturn(ENTITY_UUID_STRING);
		when(readRepository.getAccount(ACCOUNT_UUID_STRING)).thenReturn(account);
		when(readRepository.getAccountIdByWalletId(WALLET_UUID_STRING2)).thenReturn(ACCOUNT_UUID_STRING2);
		when(readRepository.getEntityIdByAccountId(ACCOUNT_UUID_STRING2)).thenReturn(ENTITY_UUID_STRING);
		when(readRepository.getEntityIdByWalletId(WALLET_UUID_STRING)).thenReturn(ENTITY_UUID_STRING);
		when(readRepository.getAccount(ACCOUNT_UUID_STRING2)).thenReturn(account);
		when(readRepository.getWallet(Mockito.anyString())).thenReturn(wallet);
		accountAggregate.handleMoveAssetCommand(command);
		// verify event is added into eventRepository
		verify(eventRepository, times(2)).addEvent(Mockito.anyString(), Mockito.any());
	}
}
