export default function Terminos() {
	return (
		<section style={{ display: 'grid', gap: 12, maxWidth: 900 }}>
			<h2>Términos y Alcance</h2>
			<ul>
				<li>Las búsquedas se realizan contra múltiples proveedores y se muestran unificadas por precio.</li>
				<li>Las compras se hacen vía formularios con datos requeridos por el proveedor.</li>
				<li>Las operaciones se registran en la BD del proveedor (no implementado en este demo).</li>
				<li>Cancelación: solo paquete completo desde admin en este demo.</li>
				<li>Fuera de alcance: aerolíneas mixtas o escalas mixtas.</li>
			</ul>
		</section>
	);
}
