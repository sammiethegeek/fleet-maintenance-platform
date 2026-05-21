import axios, { AxiosError } from 'axios';
import cors from 'cors';
import express, { NextFunction, Request, Response } from 'express';
import jwt from 'jsonwebtoken';

const app = express();
const port = Number(process.env.BFF_PORT ?? 3000);
const backendBaseUrl = process.env.BACKEND_BASE_URL ?? 'http://localhost:8080/api';
const jwtSecret = process.env.JWT_SECRET ?? 'mysecretkey123456789012345678901234567890';

type AuthenticatedRequest = Request & {
  user?: {
    id: string;
    name: string;
    role: string;
  };
};

app.use(cors({ origin: process.env.ALLOWED_ORIGINS?.split(',') ?? '*' }));
app.use(express.json());

const backend = axios.create({
  baseURL: backendBaseUrl,
  timeout: 10000
});

function normalizeRole(role: string | undefined): 'COORDINATOR' | 'PROVIDER' {
  return role === 'ROLE_PROVIDER' ? 'PROVIDER' : 'COORDINATOR';
}

function authMiddleware(req: AuthenticatedRequest, res: Response, next: NextFunction) {
  const authorization = req.header('authorization');
  if (!authorization?.startsWith('Bearer ')) {
    res.status(401).json({ message: 'Missing bearer token' });
    return;
  }

  try {
    const token = authorization.slice(7);
    const claims = jwt.verify(token, jwtSecret) as jwt.JwtPayload;
    req.user = {
      id: String(claims.id ?? claims.sub),
      name: String(claims.name ?? claims.sub),
      role: String(claims.role)
    };
    next();
  } catch {
    res.status(401).json({ message: 'Invalid bearer token' });
  }
}

function authHeader(req: Request) {
  return { Authorization: req.header('authorization') ?? '' };
}

function handleProxyError(error: unknown, res: Response) {
  const axiosError = error as AxiosError;
  if (axiosError.response) {
    res.status(axiosError.response.status).json(axiosError.response.data);
    return;
  }
  res.status(500).json({ message: 'BFF proxy error' });
}

app.post('/api/auth/login', async (req, res) => {
    console.log("Calling");
  try {
    const response = await backend.post('/auth/login', req.body);
    const token = response.data.token as string;
    console.log(response);
    const claims = jwt.verify(token, jwtSecret) as jwt.JwtPayload;
    res.json({
      username: response.data.username,
      token,
      role: normalizeRole(String(claims.role))
    });
  } catch (error) {
    handleProxyError(error, res);
  }
});

app.get('/api/dashboard', authMiddleware, async (req: Request, res: Response) => {
  try {
    const authenticatedReq = req as AuthenticatedRequest;
    const response = await backend.get('/dashboard', { headers: authHeader(req) });
    res.json({
      ...response.data,
      role: normalizeRole(authenticatedReq.user?.role)
    });
  } catch (error) {
    handleProxyError(error, res);
  }
});

app.get('/api/maintenance-requests/:id', authMiddleware, async (req, res) => {
  try {
    const response = await backend.get(`/maintenance-requests/${req.params.id}`, { headers: authHeader(req) });
    res.json(response.data);
  } catch (error) {
    handleProxyError(error, res);
  }
});

app.post('/api/maintenance-requests', authMiddleware, async (req, res) => {
  try {
    const response = await backend.post('/maintenance-requests', {
      ...req.body,
      createdOn: new Date().toISOString()
    }, { headers: authHeader(req) });
    res.status(201).json(response.data);
  } catch (error) {
    handleProxyError(error, res);
  }
});

app.put('/api/maintenance-requests/:id/assign-provider', authMiddleware, async (req, res) => {
  try {
    const response = await backend.put(`/maintenance-requests/${req.params.id}/assign-provider`, {
      maintenanceId: req.params.id,
      providerId: req.body.providerId ?? 'provider',
      providerName: req.body.providerName ?? 'Provider',
      updatedOn: new Date().toISOString()
    }, { headers: authHeader(req) });
    res.json(response.data);
  } catch (error) {
    handleProxyError(error, res);
  }
});

app.post('/api/maintenance-requests/:id/inspection', authMiddleware, async (req, res) => {
  try {
    const inspectedOn = req.body.inspectionDate
      ? new Date(`${req.body.inspectionDate}T00:00:00`).toISOString()
      : new Date().toISOString();
    const response = await backend.post(`/maintenance-requests/${req.params.id}/inspection`, {
      maintenanceId: req.params.id,
      updatedOn: new Date().toISOString(),
      inspectionReport: req.body.findings,
      estimatedCost: Number(req.body.estimatedCost),
      inspectedOn,
      estimatedCompletionDate: new Date(Date.now() + 24 * 60 * 60 * 1000).toISOString(),
      additionalDetails: `Estimated time: ${req.body.estimatedTime ?? 'Not provided'}`
    }, { headers: authHeader(req) });
    res.json(response.data);
  } catch (error) {
    handleProxyError(error, res);
  }
});

app.post('/api/maintenance-requests/:id/decision', authMiddleware, async (req, res) => {
  try {
    const response = await backend.post(`/maintenance-requests/${req.params.id}/decision`, {
      maintenanceId: req.params.id,
      decisionType: req.body.decisionType,
      remarks: req.body.remarks,
      updatedOn: new Date().toISOString()
    }, { headers: authHeader(req) });
    res.json(response.data);
  } catch (error) {
    handleProxyError(error, res);
  }
});

app.listen(port, () => {
  console.log(`Fleet maintenance BFF listening on http://localhost:${port}`);
});
