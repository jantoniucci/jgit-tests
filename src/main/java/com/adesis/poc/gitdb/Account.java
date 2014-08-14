package com.adesis.poc.gitdb;

import static com.adesis.poc.gitdb.GitHelper.cloneAndBranchRepository;
import static com.adesis.poc.gitdb.GitHelper.cloneRepository;
import static com.adesis.poc.gitdb.GitHelper.createBareRepository;
import static com.adesis.poc.gitdb.GitHelper.getRepoUri;
import static com.adesis.poc.gitdb.GitHelper.pushToRemoteRepository;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import org.apache.commons.io.FileUtils;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.MergeResult;
import org.eclipse.jgit.merge.MergeStrategy;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Account {

	String accountId;
	Double balance;
	List<AccountTransaction> transactions;

	@JsonIgnore
	final static ObjectMapper mapper = new ObjectMapper();

	public static Account createNewAccount(String accountId) throws Exception {
		try {
			final Account account = new Account(accountId, 0.0, new ArrayList<AccountTransaction>());
			final String targetPath = "/tmp/account-clones/" + UUID.randomUUID().toString();
			
			createBareRepository(getRepoUri(accountId));
			final Git git = cloneRepository(getRepoUri(accountId), targetPath);
			writeAccountInfo(account, targetPath);
			pushToRemoteRepository(git, "Account intialized.");
			return account;
		} catch (Exception e) {
			throw new Exception("Could not create the new Account: " + e.toString(), e);
		}
	}

	private static void writeAccountInfo(Account account, String targetPath) throws JsonGenerationException, JsonMappingException, IOException {
		mapper.writeValue(new File(targetPath + "/account.json"), account);
	}

	public static String addTransaction(String accountId, AccountTransaction accountTransaction) throws Exception {
		try {
			final String targetPath = "/tmp/account-clones/" + UUID.randomUUID().toString();
			final String operationId = "addTransaction-" + accountTransaction.getId();
			final Git git = cloneAndBranchRepository(getRepoUri(accountId), targetPath, operationId );
			Account account = readAccountInfo(targetPath);
			account.getTransactions().add(accountTransaction);
			updateBalance(accountTransaction, account);
			writeAccountInfo(account, targetPath);
			pushToRemoteRepository(git, "Transacion added.");
			return operationId;
		} catch (Exception e) {
			throw new Exception("Could not create the new Account: " + e.toString(), e);
		}
	}

	private static void updateBalance(AccountTransaction accountTransaction,
			Account account) {
		account.setBalance( account.getBalance() + accountTransaction.getAmmount() );
		accountTransaction.setBalance( account.getBalance() );
	}

	private static Account readAccountInfo(final String targetPath)
			throws IOException, JsonParseException, JsonMappingException {
		return (Account) mapper.readValue(new File(targetPath + "/account.json"), Account.class);
	}

	public static void commitOperation(String accountId, String operationId) throws Exception {
		try {
			final String targetPath = "/tmp/account-clones/" + UUID.randomUUID().toString();
			final Git git = cloneRepository(getRepoUri(accountId), targetPath);
			MergeResult mergeResult = git
					.merge()
					.include( git.getRepository().getRef("remotes/origin/" + operationId))
					.setStrategy(MergeStrategy.RESOLVE)
					.call();
			if ( mergeResult.getConflicts() != null ) {
				throw new IllegalAccessError("Could not merge : " + mergeResult.getMergeStatus() + " - " + mergeResult.getConflicts() );
			}
			git.push().setPushAll().setRemote("origin").call();
			git.close();
			FileUtils.deleteDirectory( git.getRepository().getDirectory() );
		} catch (Exception e) {
			throw new Exception("Could not create the new Account: " + e.toString(), e);
		}
		
	}

}
