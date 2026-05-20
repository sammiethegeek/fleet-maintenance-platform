# Smart Fleet Maintenance Demo

This workspace now contains:

- Spring Boot backend: repository root
- BFF: `bff/`
- Angular frontend: `frontend/`

## Start Order

1. Start PostgreSQL/Kafka/backend from the root project.
2. Start the BFF:

```bash
cd bff
npm install
npm run dev
```

3. Start Angular:

```bash
cd frontend
npm install
npm start
```

Open `http://localhost:4200`.

## Demo Flow

1. Login as `coordinator` / `coordinator123`.
2. Create a maintenance request.
3. Open the row and assign it to the demo provider.
4. Logout and login as `provider` / `provider123`.
5. Submit inspection for the assigned request.
6. Logout and login as coordinator again.
7. Open the pending request and approve, reject, or request more info.
