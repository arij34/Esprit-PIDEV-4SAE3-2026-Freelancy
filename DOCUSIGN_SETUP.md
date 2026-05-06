# DocuSign eSignature Integration Setup Guide

## Overview
This document provides a complete setup guide for integrating DocuSign digital signature capabilities into the Freelancy contract management system.

## Architecture Overview

```
┌─────────────────────────────────────────────────────────────────┐
│                     Angular Frontend (Port 4200)                │
│  - Contract Detail View                                          │
│  - "Send for Signature" Button                                  │
│  - Signing Status Display                                       │
│  - Signed Document Download                                     │
└──────────────────────┬──────────────────────────────────────────┘
                       │
                       ↓ HTTP REST API Calls
┌──────────────────────────────────────────────────────────────────┐
│              Spring Boot Backend (Port 8097)                    │
│  ┌────────────────────────────────────────────────────────────┐ │
│  │ Contract Service                                           │ │
│  │ - Send contract for signature via DocuSign API            │ │
│  │ - Get signature status                                    │ │
│  │ - Download signed document                                │ │
│  │ - Handle webhooks from DocuSign                           │ │
│  └────────────────────────────────────────────────────────────┘ │
│  ┌────────────────────────────────────────────────────────────┐ │
│  │ Contract Entity (MySQL)                                   │ │
│  │ - envelopeId (DocuSign envelope ID)                       │ │
│  │ - signatureStatus (PENDING/SIGNED/REJECTED/EXPIRED)       │ │
│  │ - signingUrl (URL for signer to sign)                     │ │
│  │ - signedAt (timestamp when signed)                        │ │
│  │ - signedDocumentUrl (URL to signed PDF)                   │ │
│  └────────────────────────────────────────────────────────────┘ │
└──────────────────────┬──────────────────────────────────────────┘
                       │
                       ↓ REST API Calls with OAuth2 Token
┌──────────────────────────────────────────────────────────────────┐
│              DocuSign eSignature API                            │
│              (https://demo.docusign.net/restapi)                │
│  - Create envelope (send contract for signature)               │
│  - Get envelope status (check signature progress)              │
│  - Get recipient view URL (for embedded signing)               │
│  - Download documents (get signed PDF)                         │
└──────────────────────────────────────────────────────────────────┘
```

---

## Implementation Checklist

### Backend (COMPLETED ✅)

- [x] Add DocuSign dependencies to `pom.xml`
- [x] Create `SignatureStatus` enum
- [x] Update `Contract` entity with signature tracking fields:
  - `envelopeId` - DocuSign envelope ID
  - `signatureStatus` - Current signature status
  - `signingUrl` - URL for recipient to sign
  - `signedAt` - Timestamp of signature
  - `signedDocumentUrl` - URL to signed document
- [x] Create `DocuSignConfig` configuration class
- [x] Create `IDocuSignService` interface
- [x] Create `DocuSignServiceImpl` service with methods:
  - `sendForSignature()` - Send contract to DocuSign
  - `getSignatureStatus()` - Check signing status
  - `downloadSignedDocument()` - Get signed PDF
  - `handleSignatureWebhook()` - Process DocuSign callbacks
  - `voidEnvelope()` - Cancel signature request
- [x] Add repository method `findByEnvelopeId()`
- [x] Add REST endpoints:
  - `POST /api/contracts/{id}/send-for-signature`
  - `GET /api/contracts/{id}/signature-status`
  - `POST /api/contracts/signature-webhook`
  - `GET /api/contracts/{id}/download-signed`
- [x] Add configuration properties to `application.properties`
- [x] Verify backend compilation

### Frontend (TO DO)

- [ ] Create signature button component
- [ ] Create signature modal/dialog
- [ ] Display signing status (PENDING/SIGNED/REJECTED)
- [ ] Add "View Signing URL" button
- [ ] Add "Download Signed Document" button
- [ ] Update contract status display in list/detail views
- [ ] Handle signing URL redirects
- [ ] Add loading states and error handling

---

## Backend Configuration

### 1. Dependencies (Already Added)

The backend uses Spring Boot's built-in `RestTemplate` for HTTP calls to DocuSign API. No external SDK dependencies are required.

### 2. Application Properties

Add the following to `src/main/resources/application.properties`:

```properties
# DocuSign eSignature Configuration
# API Endpoint
docusign.base-path=https://demo.docusign.net/restapi

# DocuSign Account ID (from DocuSign dashboard)
docusign.account-id=YOUR_ACCOUNT_ID

# OAuth2 Credentials (from DocuSign developer console)
docusign.client-id=YOUR_CLIENT_ID
docusign.client-secret=YOUR_CLIENT_SECRET

# User ID for impersonation (Service Account User ID)
docusign.impersonated-user-id=YOUR_IMPERSONATED_USER_ID

# Callback URL (where user returns after signing)
docusign.return-url=http://localhost:4200/contracts
```

### 3. Database Migration

Run this SQL to add signature tracking columns to the contracts table:

```sql
ALTER TABLE contracts ADD COLUMN docusign_envelope_id VARCHAR(255);
ALTER TABLE contracts ADD COLUMN signature_status VARCHAR(50);
ALTER TABLE contracts ADD COLUMN signing_url LONGTEXT;
ALTER TABLE contracts ADD COLUMN signed_at DATETIME;
ALTER TABLE contracts ADD COLUMN signed_document_url LONGTEXT;

-- Create index on envelope_id for faster queries
CREATE INDEX idx_envelope_id ON contracts(docusign_envelope_id);
```

---

## DocuSign Setup Instructions

### Step 1: Create DocuSign Developer Account

1. Go to https://developer.docusign.com/
2. Click "Sign Up"
3. Fill in the registration form
4. Verify your email
5. Login to your developer account

### Step 2: Create an Integration Key

1. In Developer Console, go to **Apps and Keys**
2. Click **"Create App"**
3. Enter app name (e.g., "Freelancy Contract Signing")
4. Choose **"Traditional Server-to-Server Application"** (for backend integration)
5. Click **"Create App"**
6. Go to **"Authentication"** section
7. Under **"API Account ID"**, copy your **Account ID** (docusign.account-id)

### Step 3: Generate RSA Key Pair (For JWT Authentication)

1. In the same **Authentication** section, find **"Generate RSA"** button
2. Click it to generate a public/private key pair
3. The private key will be displayed - **SAVE IT SECURELY**
4. Note the **Integration Key** (docusign.client-id)

### Step 4: Configure Impersonation

1. In the Developer Console, go to **"Users"**
2. Find your user account
3. Note the **User ID** (docusign.impersonated-user-id)

### Step 5: Update Configuration

Update `application.properties` with:
- `docusign.account-id` -> Your Account ID
- `docusign.client-id` -> Your Integration Key
- `docusign.impersonated-user-id` -> Your User ID

---

## REST API Endpoints

### 1. Send Contract for Signature

**Endpoint:** `POST /api/contracts/{contractId}/send-for-signature`

**Request Body:**
```json
{
  "signerEmail": "freelancer@example.com",
  "signerName": "John Doe"
}
```

**Response:**
```json
{
  "message": "Contract sent for signature",
  "contractId": 1,
  "envelopeId": "12345678-abcd-1234-abcd-1234567890ab",
  "signingUrl": "https://demo.docusign.net/..."
}
```

**Frontend Usage:**
```typescript
// Send contract for eSignature
sendForSignature(contractId: number, signerEmail: string, signerName: string) {
  const body = { signerEmail, signerName };
  return this.http.post(`/api/contracts/${contractId}/send-for-signature`, body);
}
```

### 2. Get Signature Status

**Endpoint:** `GET /api/contracts/{contractId}/signature-status`

**Response:**
```json
{
  "contractId": 1,
  "envelopeId": "12345678-abcd-1234-abcd-1234567890ab",
  "signatureStatus": "PENDING",
  "docuSignStatus": "sent",
  "signedAt": null
}
```

**Frontend Usage:**
```typescript
// Check signature status
getSignatureStatus(contractId: number) {
  return this.http.get(`/api/contracts/${contractId}/signature-status`);
}
```

### 3. Download Signed Document

**Endpoint:** `GET /api/contracts/{contractId}/download-signed`

**Response:** Downloads PDF file

**Frontend Usage:**
```typescript
// Download the signed document
downloadSignedDocument(contractId: number) {
  return this.http.get(
    `/api/contracts/${contractId}/download-signed`,
    { responseType: 'blob' }
  );
}
```

### 4. Webhook Handler (DocuSign Callbacks)

**Endpoint:** `POST /api/contracts/signature-webhook`

This endpoint is called by DocuSign when signature status changes. The webhook payload should include the `envelopeId`.

**Request Body:**
```json
{
  "envelopeId": "12345678-abcd-1234-abcd-1234567890ab"
}
```

---

## Frontend Implementation

### Service Methods

Add these methods to your contract service:

```typescript
// send-for-signature.ts
sendForSignature(contractId: number, signerEmail: string, signerName: string): Observable<any> {
  return this.http.post(`/api/contracts/${contractId}/send-for-signature`, {
    signerEmail,
    signerName
  });
}

getSignatureStatus(contractId: number): Observable<any> {
  return this.http.get(`/api/contracts/${contractId}/signature-status`);
}

downloadSignedDocument(contractId: number) {
  return this.http.get(
    `/api/contracts/${contractId}/download-signed`,
    { responseType: 'blob' }
  );
}
```

### Component Integration

Example component code:

```typescript
export class ContractDetailComponent {
  signingInProgress = false;
  signatureStatus: string = 'UNSIGNED';

  constructor(private contractService: ContractService) {}

  sendForSignature() {
    this.signingInProgress = true;
    
    this.contractService.sendForSignature(
      this.contract.id,
      this.freelancer.email,
      this.freelancer.name
    ).subscribe({
      next: (response) => {
        console.log('Contract sent for signature:', response);
        // Redirect to signing URL or open in modal
        window.open(response.signingUrl, '_blank');
        
        // Start polling for status updates
        this.pollSignatureStatus();
      },
      error: (error) => {
        console.error('Error sending for signature:', error);
        this.signingInProgress = false;
      }
    });
  }

  pollSignatureStatus() {
    // Poll every 5 seconds for status updates
    const statusCheck = setInterval(() => {
      this.contractService.getSignatureStatus(this.contract.id).subscribe({
        next: (response) => {
          this.signatureStatus = response.signatureStatus;
          
          if (response.signatureStatus === 'SIGNED') {
            clearInterval(statusCheck);
            this.signingInProgress = false;
            // Refresh contract data
            this.loadContract();
          }
        }
      });
    }, 5000);
  }

  downloadSignedDocument() {
    this.contractService.downloadSignedDocument(this.contract.id).subscribe({
      next: (blob) => {
        const url = window.URL.createObjectURL(blob);
        const link = document.createElement('a');
        link.href = url;
        link.download = `${this.contract.title}_signed.pdf`;
        link.click();
      }
    });
  }
}
```

---

## JWT Authentication Flow (Production)

For production use, implement JWT Bearer authentication:

```java
private String getAccessToken() throws IOException {
    // 1. Create JWT assertion
    String jwtAssertion = createJWT(
        docuSignConfig.getClientId(),
        docuSignConfig.getImpersonatedUserId()
    );
    
    // 2. Exchange JWT for access token
    String tokenUrl = docuSignConfig.getBasePath() + "/oauth/token";
    
    Map<String, String> body = new LinkedHashMap<>();
    body.put("grant_type", "urn:ietf:params:oauth:grant-type:jwt-bearer");
    body.put("assertion", jwtAssertion);

    // 3. POST to DocuSign OAuth endpoint
    // 4. Extract and return access_token
    
    return accessToken;
}

private String createJWT(String clientId, String userId) {
    // Use java.util.JWT or similar library to create JWT token
    // Sign with RSA private key from DocuSign
}
```

---

## Testing the Integration

### 1. Manual Test with Postman

1. **Send for Signature:**
   ```
   POST http://localhost:8097/api/contracts/1/send-for-signature
   Body: {"signerEmail": "test@example.com", "signerName": "Test User"}
   ```

2. **Check Status:**
   ```
   GET http://localhost:8097/api/contracts/1/signature-status
   ```

3. **Download Signed Document:**
   ```
   GET http://localhost:8097/api/contracts/1/download-signed
   ```

### 2. Signature Flow Test

1. Create a test contract
2. Send it for signature using the endpoint
3. Click the returned signing URL
4. Sign the document in DocuSign
5. Verify status changes to SIGNED
6. Download the signed document

---

## Troubleshooting

### Common Issues

**1. "Missing DocuSign Credentials"**
- Ensure all properties are set in `application.properties`
- Verify Account ID, Client ID, and User ID are correct

**2. "Invalid Bearer Token"**
- JWT authentication flow not implemented yet
- Placeholder token is used in current implementation
- Implement proper JWT flow for production

**3. "Contract PDF Not Found"**
- Ensure `contract.pdfUrl` is correctly set
- File path must exist on the server

**4. "Webhook Not Received"**
- Configure webhook URL in DocuSign dashboard
- Ensure firewall allows DocuSign servers to reach your backend
- Add signing URL callback in envelope configuration

---

## Production Checklist

- [ ] Implement JWT Bearer authentication (not placeholder)
- [ ] Use production DocuSign endpoint (https://na3.docusign.net/restapi)
- [ ] Generate and securely store RSA private key
- [ ] Configure webhook URL in DocuSign dashboard
- [ ] Set up HTTPS for all endpoints
- [ ] Add proper error handling and logging
- [ ] Implement retry logic for API calls
- [ ] Add rate limiting for signature requests
- [ ] Configure secure storage for API credentials
- [ ] Test complete signing workflow end-to-end
- [ ] Add audit logging for signature events
- [ ] Configure email notifications for parties

---

## Documentation Links

- [DocuSign API Documentation](https://developers.docusign.com/docs/esign-rest-api/)
- [DocuSign JWT Authentication](https://developers.docusign.com/docs/esign-rest-api/esign101/jwt-auth/)
- [DocuSign Webhooks](https://developers.docusign.com/docs/esign-rest-api/connect/connectwebhooks/)
- [Spring Boot RestTemplate Documentation](https://spring.io/guides/gs/consuming-rest/)

---

## Support

For issues or questions about the DocuSign integration:
1. Check the [DocuSign API status page](https://status.docusign.com/)
2. Review DocuSign developer console logs
3. Enable debug logging in Spring Boot
4. Check contract service logs for detailed error messages
