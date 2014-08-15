package com.adesis.poc.gitdb;

import static com.adesis.poc.gitdb.ProductGitHelper.cloneRepository;

import java.io.File;
import java.util.List;
import java.util.UUID;

import org.apache.commons.io.FileUtils;
import org.codehaus.jackson.map.ObjectMapper;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.lib.Ref;
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
		
		final Git git = cloneRepository(accountId);
		final ObjectMapper mapper = new ObjectMapper();
		Account account = (Account) mapper.readValue(new File("/tmp/account-clones/" + accountId + "/account.json"), Account.class);
		Assert.assertEquals(accountId, account.getAccountId());
		Assert.assertEquals(1, account.getTransactions().size());
		List<Ref> branchList = git.branchList().call();
		Assert.assertEquals(1, branchList.size() );
		Assert.assertEquals("refs/heads/master", branchList.get(0).getName() );
		FileUtils.deleteDirectory(new File("/tmp/account-clones/" + accountId));
		FileUtils.deleteDirectory(new File("/tmp/account-bare/" + accountId + ".git"));
	}
}
