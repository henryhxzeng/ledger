# ledger
Open Account API: Create entity and its accounts/wallets.
The creation is processed in async mode.
```
    POST http://localhost:8080/api/openAccount
    
    {
        "entityName" : "HenryEntity",
        "account" : [
        	{"accountName":"Low risk account",
        	 "wallets": [
        	 	{"walletName": "Wallet BOND 01", "type": "BOND"},
        	 	{"walletName": "Wallet BOND 02", "type": "BOND"}
        	 	]
        	},
        	{"accountName":"High risk account 02",
        	 "wallets": [
        	 	{"walletName": "Wallet STOCK 01", "type": "STOCK"},
        	 	{"walletName": "Wallet STOCK 02", "type": "STOCK"},
        	 	{"walletName": "Wallet CRYPTO", "type": "CRYPTO"}
        	 	]
        	}
        ]
    }
```

Query Entity API: Get entity information and its accounts/wallets by entityId
```
	GET http://localhost:8080/api/query/entity/{entityId}
```

Query Account API: Get account information and its wallets by accountId
```
	GET http://localhost:8080/api/query/account/{accountId}
```

Query Wallet API: Get wallet information by walletId
```
	GET http://localhost:8080/api/query/wallet/{walletId}
```

Fund In API: Move in assets to one wallet, multiple move-ins can be processed in a single request
The movement is processed in async mode.
```
 POST http://localhost:8080/api/fundIn
 [
	{"walletId": "76c0857c-7808-41f3-87db-4e43d8d5f868", "amount": 10.0},
	{"walletId": "1428921e-1bc7-4888-b2dc-762a0b1fbed8", "amount": 20.0},
	{"walletId": "1949e716-b750-47f2-a947-6cfc8c1eade2", "amount": 1000.0}	
 ]
```

Fund Out API: Move out assets from one wallet, multiple move-outs can be processed in a single request
The movement is processed in async mode.
```
 POST http://localhost:8080/api/fundOut
 [
	{"walletId": "76c0857c-7808-41f3-87db-4e43d8d5f868", "amount": 10.0},
	{"walletId": "1428921e-1bc7-4888-b2dc-762a0b1fbed8", "amount": 20.0},
	{"walletId": "1949e716-b750-47f2-a947-6cfc8c1eade2", "amount": 1000.0}	
 ]
```

Move Asset API:  Move assets from one wallet to another, multiple movements can be processed in a single request
The movement is processed in async mode.
```
 POST http://localhost:8080/api/moveAsset
[
	{"fromWalletId": "76c0857c-7808-41f3-87db-4e43d8d5f868", "toWalletId": "1949e716-b750-47f2-a947-6cfc8c1eade2", "amount": 1.0},
	{"fromWalletId": "76c0857c-7808-41f3-87db-4e43d8d5f868", "toWalletId": "b02d3789-d643-4076-8a17-76ae2b4502b2", "amount": 1.0}
]
```

Update Account Status API: Update account status from one to another
```
POST http://localhost:8080/api/updateAccountStatus
{
	"id" :"e1711419-b24b-4dba-ad7c-94afcaab3886",
	"status": "CLOSED"
}
```

Query Movement API: Query all historical movements of assets within this entity 
```
GET http://localhost:8080/api/query/movement/{entityId}
```

Query historical balance of wallet API: Query wallet balance at a given timestamp by walletId
```
POST  http://localhost:8080/api/query/walletHistoricalBalance

The datatime format shall be yyyy-MM-dd HH:mm:ssZ
{
	"id": "3bae79f5-ee31-4ce2-b51c-dd85111664d4",
	"datetime": "2024-05-14 09:11:00+0000"
}
```

