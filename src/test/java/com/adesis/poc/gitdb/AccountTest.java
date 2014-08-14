package com.adesis.poc.gitdb;

import static com.adesis.poc.gitdb.GitHelper.cloneRepository;
import static com.adesis.poc.gitdb.GitHelper.getRepoUri;

import java.io.File;
import java.util.UUID;

import org.apache.commons.io.FileUtils;
import org.codehaus.jackson.map.ObjectMapper;
import org.junit.Assert;
import org.junit.Test;

public class AccountTest {


	@Test
	public void createAnAccountAndAddInitialTransactions() throws Exception {
		final String accountId = UUID.randomUUID().toString();
		final String transacionId = UUID.randomUUID().toString();
		Account.createNewAccount(accountId);
		final String operationId = Account.addTransaction(accountId, new AccountTransaction(transacionId, "2014-08-12", "Initial transaction", 1234.56, 0.0));
		Account.commitOperation(accountId, operationId);
		
		cloneRepository(getRepoUri(accountId), "/tmp/account-tests/" + accountId);
		final ObjectMapper mapper = new ObjectMapper();
		Account account = (Account) mapper.readValue(new File("/tmp/account-tests/" + accountId + "/account.json"), Account.class);
		Assert.assertEquals(accountId, account.getAccountId());
		Assert.assertEquals(1, account.getTransactions().size());
		FileUtils.deleteDirectory(new File("/tmp/account-tests/" + accountId));
	}
}
