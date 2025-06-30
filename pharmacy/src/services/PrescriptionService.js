import axios from 'axios';
import ApiService from './ApiService';

/**
 * @typedef {Object} PrescriptionData
 * @property {string} approvalCode - Código de aprobación del seguro
 * @property {Array<MedicationItem>} medications - Lista de medicamentos a procesar
 * @property {number} total - Monto total de la receta
 * @property {string} [patientName] - Nombre del paciente
 * @property {string} [doctorName] - Nombre del doctor
 * @property {Date} [prescriptionDate] - Fecha de la receta
 */

/**
 * @typedef {Object} MedicationItem
 * @property {string} code - Código del medicamento
 * @property {string} name - Nombre del medicamento
 * @property {number} quantity - Cantidad a dispensar
 * @property {number} price - Precio unitario
 * @property {string} [instructions] - Instrucciones de uso
 * @property {string} [dosage] - Dosis prescrita
 * @property {string} [frequency] - Frecuencia de administración
 */

/**
 * @typedef {Object} PrescriptionVerification
 * @property {boolean} success - Indica si la verificación fue exitosa
 * @property {string} [message] - Mensaje de respuesta
 * @property {number} [prescriptionTotal] - Monto total aprobado por el seguro
 * @property {string} [patientName] - Nombre del paciente
 * @property {string} [insuranceProvider] - Proveedor de seguro
 * @property {Date} [expirationDate] - Fecha de expiración de la aprobación
 */

/**
 * @typedef {Object} InventoryCheck
 * @property {boolean} available - Indica si hay stock disponible
 * @property {number} currentStock - Stock actual disponible
 * @property {number} requestedQuantity - Cantidad solicitada
 * @property {string} [message] - Mensaje informativo
 */

/**
 * @typedef {Object} ReceiptData
 * @property {string} prescriptionId - ID de la receta
 * @property {Array<MedicationItem>} medications - Medicamentos dispensados
 * @property {number} total - Monto total
 * @property {string} patientName - Nombre del paciente
 * @property {string} [pharmacistName] - Nombre del farmacéutico
 * @property {Date} [dispensingDate] - Fecha de dispensación
 */

/**
 * Servicio para gestionar el procesamiento de recetas médicas
 * 
 * Este servicio maneja toda la lógica relacionada con:
 * - Verificación de recetas con el sistema de seguros
 * - Procesamiento y dispensación de medicamentos
 * - Control de inventario
 * - Generación de recibos
 * - Integración con el sistema de seguros
 * 
 * @example
 * // Verificar una receta
 * const verification = await PrescriptionService.verifyPrescription('APP123');
 * if (verification.success) {
 *   console.log('Receta aprobada:', verification.prescriptionTotal);
 * }
 * 
 * @example
 * // Procesar una receta completa
 * const prescriptionData = {
 *   approvalCode: 'APP123',
 *   medications: [
 *     { code: 'MED001', name: 'Paracetamol', quantity: 2, price: 5.50 }
 *   ],
 *   total: 11.00
 * };
 * const result = await PrescriptionService.processPrescription(prescriptionData);
 */
class PrescriptionService {
  /**
   * Verifica una receta médica usando el código de aprobación del seguro
   * 
   * Esta función se comunica con el sistema de seguros para validar:
   * - Si el código de aprobación es válido
   * - Si la receta está vigente
   * - El monto total aprobado
   * - Información del paciente y seguro
   * 
   * @param {string} approvalCode - Código de aprobación del seguro (ej: 'APP123456')
   * @returns {Promise<PrescriptionVerification>} Resultado de la verificación
   * @throws {Error} Si hay un error de conexión o el código es inválido
   * 
   * @example
   * try {
   *   const result = await PrescriptionService.verifyPrescription('APP123456');
   *   if (result.success) {
   *     console.log('Receta válida, monto aprobado:', result.prescriptionTotal);
   *   } else {
   *     console.log('Receta rechazada:', result.message);
   *   }
   * } catch (error) {
   *   console.error('Error al verificar receta:', error.message);
   * }
   */
  async verifyPrescription(approvalCode) {
    try {
      const response = await axios.get(ApiService.getEnsuranceApiUrl(`/service-approvals/check/${approvalCode}`));
      return response.data;
    } catch (error) {
      console.error('Error al verificar la receta:', error);
      throw error;
    }
  }

  /**
   * Procesa una receta para dispensar medicamentos
   * 
   * Esta función realiza el proceso completo de dispensación:
   * - Valida que todos los medicamentos estén disponibles
   * - Actualiza el inventario
   * - Genera el recibo
   * - Registra la transacción
   * 
   * @param {PrescriptionData} prescriptionData - Datos completos de la receta a procesar
   * @returns {Promise<Object>} Resultado del procesamiento con número de recibo
   * @throws {Error} Si hay error en el procesamiento o falta stock
   * 
   * @example
   * const prescriptionData = {
   *   approvalCode: 'APP123456',
   *   medications: [
   *     { code: 'MED001', name: 'Ibuprofeno', quantity: 1, price: 8.50 },
   *     { code: 'MED002', name: 'Paracetamol', quantity: 2, price: 5.25 }
   *   ],
   *   total: 18.00
   * };
   * 
   * const result = await PrescriptionService.processPrescription(prescriptionData);
   * console.log('Recibo generado:', result.receiptNumber);
   */
  async processPrescription(prescriptionData) {
    try {
      const response = await axios.post(ApiService.getPharmacyApiUrl('/prescriptions/process'), prescriptionData);
      return response.data;
    } catch (error) {
      console.error('Error al procesar la receta:', error);
      throw error;
    }
  }

  /**
   * Marca una receta como completada en el sistema de seguros
   * 
   * Notifica al sistema de seguros que la receta ha sido procesada
   * y los medicamentos han sido dispensados exitosamente.
   * 
   * @param {string} approvalCode - Código de aprobación del seguro
   * @returns {Promise<Object>} Confirmación de completado
   * @throws {Error} Si hay error al marcar como completada
   * 
   * @example
   * await PrescriptionService.completePrescription('APP123456');
   * console.log('Receta marcada como completada en el sistema de seguros');
   */
  async completePrescription(approvalCode) {
    try {
      const response = await axios.put(ApiService.getEnsuranceApiUrl(`/service-approvals/complete/${approvalCode}`));
      return response.data;
    } catch (error) {
      console.error('Error al completar la receta:', error);
      throw error;
    }
  }

  /**
   * Obtiene el historial de recetas procesadas
   * 
   * Permite consultar el historial de recetas con filtros opcionales:
   * - Por fecha
   * - Por paciente
   * - Por estado
   * - Por monto
   * 
   * @param {Object} filters - Filtros para la búsqueda
   * @param {Date} [filters.startDate] - Fecha de inicio
   * @param {Date} [filters.endDate] - Fecha de fin
   * @param {string} [filters.patientName] - Nombre del paciente
   * @param {string} [filters.status] - Estado de la receta
   * @param {number} [filters.minAmount] - Monto mínimo
   * @param {number} [filters.maxAmount] - Monto máximo
   * @returns {Promise<Array<Object>>} Lista de recetas procesadas
   * @throws {Error} Si hay error al obtener el historial
   * 
   * @example
   * // Obtener recetas de la última semana
   * const lastWeek = new Date();
   * lastWeek.setDate(lastWeek.getDate() - 7);
   * 
   * const history = await PrescriptionService.getPrescriptionHistory({
   *   startDate: lastWeek,
   *   endDate: new Date()
   * });
   * console.log('Recetas procesadas:', history.length);
   */
  async getPrescriptionHistory(filters = {}) {
    try {
      const response = await axios.get(ApiService.getPharmacyApiUrl('/prescriptions/history'), { 
        params: filters 
      });
      return response.data;
    } catch (error) {
      console.error('Error al obtener el historial de recetas:', error);
      throw error;
    }
  }

  /**
   * Obtiene el detalle de una receta específica
   * 
   * Recupera toda la información detallada de una receta incluyendo:
   * - Información del paciente
   * - Lista completa de medicamentos
   * - Dosis y frecuencia
   * - Estado de procesamiento
   * 
   * @param {string} prescriptionId - ID único de la receta
   * @returns {Promise<Object>} Detalles completos de la receta
   * @throws {Error} Si la receta no existe o hay error de conexión
   * 
   * @example
   * const prescription = await PrescriptionService.getPrescriptionDetails('PRESC123');
   * console.log('Paciente:', prescription.patientName);
   * console.log('Medicamentos:', prescription.medications.length);
   */
  async getPrescriptionDetails(prescriptionId) {
    try {
      const response = await axios.get(ApiService.getPharmacyApiUrl(`/prescriptions/${prescriptionId}`));
      return response.data;
    } catch (error) {
      console.error('Error al obtener detalles de la receta:', error);
      throw error;
    }
  }

  /**
   * Verifica si un medicamento está disponible en inventario
   * 
   * Valida la disponibilidad de stock antes de procesar una receta
   * para evitar problemas durante la dispensación.
   * 
   * @param {string} medicationCode - Código único del medicamento
   * @param {number} quantity - Cantidad requerida para la receta
   * @returns {Promise<InventoryCheck>} Resultado de la verificación de stock
   * @throws {Error} Si hay error al verificar el inventario
   * 
   * @example
   * const stockCheck = await PrescriptionService.checkMedicationAvailability('MED001', 5);
   * if (stockCheck.available) {
   *   console.log(`Stock disponible: ${stockCheck.currentStock} unidades`);
   * } else {
   *   console.log(`Stock insuficiente. Disponible: ${stockCheck.currentStock}`);
   * }
   */
  async checkMedicationAvailability(medicationCode, quantity) {
    try {
      const response = await axios.get(ApiService.getPharmacyApiUrl('/inventory/check'), {
        params: {
          code: medicationCode,
          quantity: quantity
        }
      });
      return response.data;
    } catch (error) {
      console.error('Error al verificar disponibilidad del medicamento:', error);
      throw error;
    }
  }

  /**
   * Actualiza el inventario después de dispensar medicamentos
   * 
   * Reduce el stock disponible después de procesar una receta
   * para mantener el inventario actualizado.
   * 
   * @param {Array<MedicationItem>} medications - Lista de medicamentos dispensados
   * @returns {Promise<Object>} Confirmación de actualización
   * @throws {Error} Si hay error al actualizar el inventario
   * 
   * @example
   * const dispensedMedications = [
   *   { code: 'MED001', quantity: 2 },
   *   { code: 'MED002', quantity: 1 }
   * ];
   * await PrescriptionService.updateInventoryAfterDispensing(dispensedMedications);
   * console.log('Inventario actualizado exitosamente');
   */
  async updateInventoryAfterDispensing(medications) {
    try {
      const response = await axios.post(ApiService.getPharmacyApiUrl('/inventory/update-after-dispensing'), {
        medications: medications
      });
      return response.data;
    } catch (error) {
      console.error('Error al actualizar el inventario:', error);
      throw error;
    }
  }

  /**
   * Genera un recibo para la receta procesada
   * 
   * Crea un recibo detallado con toda la información de la dispensación
   * para que el paciente tenga un comprobante oficial.
   * 
   * @param {ReceiptData} receiptData - Datos para generar el recibo
   * @returns {Promise<Object>} Recibo generado con número único
   * @throws {Error} Si hay error al generar el recibo
   * 
   * @example
   * const receiptData = {
   *   prescriptionId: 'PRESC123',
   *   medications: [
   *     { name: 'Ibuprofeno', quantity: 1, price: 8.50 }
   *   ],
   *   total: 8.50,
   *   patientName: 'Juan Pérez'
   * };
   * 
   * const receipt = await PrescriptionService.generateReceipt(receiptData);
   * console.log('Recibo generado:', receipt.receiptNumber);
   */
  async generateReceipt(receiptData) {
    try {
      const response = await axios.post(ApiService.getPharmacyApiUrl('/receipts/generate'), receiptData);
      return response.data;
    } catch (error) {
      console.error('Error al generar el recibo:', error);
      throw error;
    }
  }

  /**
   * Verifica si un medicamento está cubierto por el seguro
   * 
   * Consulta con el sistema de seguros para validar si un medicamento
   * específico está incluido en la cobertura del paciente.
   * 
   * @param {string} medicationCode - Código del medicamento a verificar
   * @param {string} approvalCode - Código de aprobación del seguro
   * @returns {Promise<Object>} Resultado de la verificación de cobertura
   * @throws {Error} Si hay error al verificar la cobertura
   * 
   * @example
   * const coverage = await PrescriptionService.checkInsuranceCoverage('MED001', 'APP123456');
   * if (coverage.covered) {
   *   console.log('Medicamento cubierto al', coverage.coveragePercentage + '%');
   * } else {
   *   console.log('Medicamento no cubierto por el seguro');
   * }
   */
  async checkInsuranceCoverage(medicationCode, approvalCode) {
    try {
      const response = await axios.get(ApiService.getEnsuranceApiUrl('/medication-coverage/check'), {
        params: {
          medicationCode,
          approvalCode
        }
      });
      return response.data;
    } catch (error) {
      console.error('Error al verificar cobertura del medicamento:', error);
      throw error;
    }
  }
}

export default new PrescriptionService(); 