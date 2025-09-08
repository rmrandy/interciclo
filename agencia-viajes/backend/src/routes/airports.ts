import { Router } from 'express';
import { body, param } from 'express-validator';
import Airport from '../models/Airport';
import { authenticate, authorize } from '../middleware/auth';

const router = Router();

// Validaciones
const airportValidation = [
  body('code')
    .isLength({ min: 3, max: 3 })
    .withMessage('El código debe tener exactamente 3 caracteres')
    .isUppercase()
    .withMessage('El código debe estar en mayúsculas'),
  body('name')
    .notEmpty()
    .withMessage('El nombre es requerido')
    .isLength({ max: 100 })
    .withMessage('El nombre no puede exceder 100 caracteres'),
  body('city')
    .notEmpty()
    .withMessage('La ciudad es requerida')
    .isLength({ max: 50 })
    .withMessage('La ciudad no puede exceder 50 caracteres'),
  body('country')
    .notEmpty()
    .withMessage('El país es requerido')
    .isLength({ max: 50 })
    .withMessage('El país no puede exceder 50 caracteres'),
  body('coordinates.latitude')
    .isFloat({ min: -90, max: 90 })
    .withMessage('La latitud debe estar entre -90 y 90'),
  body('coordinates.longitude')
    .isFloat({ min: -180, max: 180 })
    .withMessage('La longitud debe estar entre -180 y 180')
];

const mongoIdValidation = [
  param('id')
    .isMongoId()
    .withMessage('ID inválido')
];

// Obtener todos los aeropuertos
router.get('/', async (req, res) => {
  try {
    const page = parseInt(req.query.page as string) || 1;
    const limit = parseInt(req.query.limit as string) || 50;
    const skip = (page - 1) * limit;

    const airports = await Airport.find()
      .sort({ city: 1, name: 1 })
      .skip(skip)
      .limit(limit)
      .lean();

    const total = await Airport.countDocuments();

    res.json({
      success: true,
      count: airports.length,
      total,
      page,
      pages: Math.ceil(total / limit),
      data: airports
    });
  } catch (error) {
    console.error('Error al obtener aeropuertos:', error);
    res.status(500).json({
      success: false,
      message: 'Error interno del servidor'
    });
  }
});

// Obtener aeropuerto por ID
router.get('/:id', mongoIdValidation, async (req, res) => {
  try {
    const airport = await Airport.findById(req.params.id);

    if (!airport) {
      return res.status(404).json({
        success: false,
        message: 'Aeropuerto no encontrado'
      });
    }

    res.json({
      success: true,
      data: airport
    });
  } catch (error) {
    console.error('Error al obtener aeropuerto:', error);
    res.status(500).json({
      success: false,
      message: 'Error interno del servidor'
    });
  }
});

// Buscar aeropuertos por código o ciudad
router.get('/search/:query', async (req, res) => {
  try {
    const query = req.params.query;
    const searchRegex = new RegExp(query, 'i');

    const airports = await Airport.find({
      $or: [
        { code: searchRegex },
        { name: searchRegex },
        { city: searchRegex },
        { country: searchRegex }
      ]
    })
    .sort({ city: 1, name: 1 })
    .limit(20)
    .lean();

    res.json({
      success: true,
      count: airports.length,
      data: airports
    });
  } catch (error) {
    console.error('Error en búsqueda de aeropuertos:', error);
    res.status(500).json({
      success: false,
      message: 'Error interno del servidor'
    });
  }
});

// Crear aeropuerto (solo administradores)
router.post('/', authenticate, authorize('admin'), airportValidation, async (req, res) => {
  try {
    const airport = await Airport.create(req.body);

    res.status(201).json({
      success: true,
      message: 'Aeropuerto creado exitosamente',
      data: airport
    });
  } catch (error) {
    console.error('Error al crear aeropuerto:', error);
    res.status(500).json({
      success: false,
      message: 'Error interno del servidor'
    });
  }
});

// Actualizar aeropuerto (solo administradores)
router.put('/:id', authenticate, authorize('admin'), mongoIdValidation, airportValidation, async (req, res) => {
  try {
    const airport = await Airport.findByIdAndUpdate(
      req.params.id,
      req.body,
      { new: true, runValidators: true }
    );

    if (!airport) {
      return res.status(404).json({
        success: false,
        message: 'Aeropuerto no encontrado'
      });
    }

    res.json({
      success: true,
      message: 'Aeropuerto actualizado exitosamente',
      data: airport
    });
  } catch (error) {
    console.error('Error al actualizar aeropuerto:', error);
    res.status(500).json({
      success: false,
      message: 'Error interno del servidor'
    });
  }
});

// Eliminar aeropuerto (solo administradores)
router.delete('/:id', authenticate, authorize('admin'), mongoIdValidation, async (req, res) => {
  try {
    const airport = await Airport.findByIdAndDelete(req.params.id);

    if (!airport) {
      return res.status(404).json({
        success: false,
        message: 'Aeropuerto no encontrado'
      });
    }

    res.json({
      success: true,
      message: 'Aeropuerto eliminado exitosamente'
    });
  } catch (error) {
    console.error('Error al eliminar aeropuerto:', error);
    res.status(500).json({
      success: false,
      message: 'Error interno del servidor'
    });
  }
});

export default router;
