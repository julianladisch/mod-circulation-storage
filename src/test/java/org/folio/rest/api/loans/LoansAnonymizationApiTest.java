package org.folio.rest.api.loans;

import static org.folio.rest.support.http.InterfaceUrls.loanStorageUrl;
import static org.folio.rest.support.matchers.HttpResponseStatusCodeMatchers.isNoContent;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.junit.MatcherAssert.assertThat;

import java.net.MalformedURLException;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.folio.rest.api.StorageTestSuite;
import org.folio.rest.support.ApiTests;
import org.folio.rest.support.IndividualResource;
import org.folio.rest.support.ResponseHandler;
import org.folio.rest.support.TextResponse;
import org.folio.rest.support.builders.LoanRequestBuilder;
import org.folio.rest.support.http.AssertingRecordClient;
import org.folio.rest.support.http.InterfaceUrls;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class LoansAnonymizationApiTest extends ApiTests {
  private final AssertingRecordClient loansClient = new AssertingRecordClient(
    client, StorageTestSuite.TENANT_ID, InterfaceUrls::loanStorageUrl);

  @Before
  public void beforeEach()
    throws MalformedURLException {

    StorageTestSuite.deleteAll(loanStorageUrl());
  }

  @After
  public void checkIdsAfterEach() {
    StorageTestSuite.checkForMismatchedIDs("loan");
  }

  @Test
  public void shouldDoNothingWhenNoLoansForUser()
    throws MalformedURLException,
    ExecutionException,
    InterruptedException,
    TimeoutException {

    final UUID unknownUserId = UUID.randomUUID();

    anonymizeLoansFor(unknownUserId);
  }

  @Test
  public void shouldNotAnonymizeLoansForOtherUser()
    throws MalformedURLException,
    ExecutionException,
    InterruptedException,
    TimeoutException {

    final UUID firstUserId = UUID.randomUUID();
    final UUID secondUserId = UUID.randomUUID();

    final IndividualResource otherUsersLoan = loansClient.create(
      new LoanRequestBuilder()
        .closed()
        .withUserId(firstUserId));

    anonymizeLoansFor(secondUserId);

    final IndividualResource fetchedLoan = loansClient.getById(
      otherUsersLoan.getId());

    assertThat(fetchedLoan.getJson().getString("userId"), is(firstUserId.toString()));
  }

  private void anonymizeLoansFor(UUID userId)
    throws MalformedURLException,
    InterruptedException,
    ExecutionException,
    TimeoutException {

    final CompletableFuture<TextResponse> postCompleted = new CompletableFuture<>();

    client.post(loanStorageUrl("/anonymize/" + userId),
      StorageTestSuite.TENANT_ID, ResponseHandler.text(postCompleted));

    final TextResponse postResponse = postCompleted.get(5, TimeUnit.SECONDS);

    assertThat(postResponse, isNoContent());
  }
}
