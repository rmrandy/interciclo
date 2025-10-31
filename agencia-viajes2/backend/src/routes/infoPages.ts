import { Router, type Request, type Response, type NextFunction } from 'express';
import InfoPage from '../models/InfoPage';
import connectDB from '../utils/database';

const router = Router();

router.use(async (_req: Request, _res: Response, next: NextFunction): Promise<void> => { await connectDB(); next(); });

// Listar páginas (opcionalmente por categoría)
router.get('/', async (req: Request, res: Response, next: NextFunction) => {
  try {
    const { category } = req.query as { category?: string };
    const filter: any = {};
    if (category) filter.category = category;
    const pages = await InfoPage.find(filter).sort({ updatedAt: -1 }).lean();
    return res.json({ success: true, data: pages });
  } catch (err) { return next(err as any); }
});

// Obtener por slug
router.get('/slug/:slug', async (req: Request, res: Response, next: NextFunction) => {
  try {
    const page = await InfoPage.findOne({ slug: req.params.slug }).lean();
    if (!page) return res.status(404).json({ success: false, message: 'No encontrado' });
    return res.json({ success: true, data: page });
  } catch (err) { return next(err as any); }
});

// Crear
router.post('/', async (req: Request, res: Response, next: NextFunction) => {
  try {
    const created = await InfoPage.create(req.body);
    return res.status(201).json({ success: true, data: created });
  } catch (err) { return next(err as any); }
});

// Actualizar por slug
router.put('/slug/:slug', async (req: Request, res: Response, next: NextFunction) => {
  try {
    const updated = await InfoPage.findOneAndUpdate({ slug: req.params.slug }, req.body, { new: true, upsert: false });
    if (!updated) return res.status(404).json({ success: false, message: 'No encontrado' });
    return res.json({ success: true, data: updated });
  } catch (err) { return next(err as any); }
});

// Eliminar por slug
router.delete('/slug/:slug', async (req: Request, res: Response, next: NextFunction) => {
  try {
    const deleted = await InfoPage.findOneAndDelete({ slug: req.params.slug });
    if (!deleted) return res.status(404).json({ success: false, message: 'No encontrado' });
    return res.json({ success: true });
  } catch (err) { return next(err as any); }
});

export default router;


