import { Router } from 'express';
import { proxyAllFlights, proxyCities } from '../controllers/airlineController';

const router = Router();

// Integración con servicio de Aerolínea
router.get('/flights', proxyAllFlights);
router.get('/cities', proxyCities);

export default router;


