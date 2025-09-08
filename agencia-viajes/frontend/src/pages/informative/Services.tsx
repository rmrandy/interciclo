import React from 'react';

const Services: React.FC = () => {
  const services = [
    {
      title: "Reservas de Vuelos",
      description: "Encuentra y reserva vuelos a los mejores destinos del mundo con las aerolíneas más confiables.",
      features: [
        "Búsqueda en tiempo real",
        "Precios competitivos",
        "Múltiples aerolíneas",
        "Reservas instantáneas"
      ],
      icon: "✈️"
    },
    {
      title: "Asistencia al Viajero",
      description: "Servicio de atención 24/7 para resolver cualquier inconveniente durante tu viaje.",
      features: [
        "Atención 24/7",
        "Soporte multilingüe",
        "Resolución rápida",
        "Seguimiento personalizado"
      ],
      icon: "🛟"
    },
    {
      title: "Seguros de Viaje",
      description: "Protege tu inversión con nuestros seguros de viaje que cubren imprevistos.",
      features: [
        "Cobertura médica",
        "Cancelación de viaje",
        "Equipaje perdido",
        "Asistencia legal"
      ],
      icon: "🛡️"
    },
    {
      title: "Traslados Aeropuerto",
      description: "Servicio de traslado desde y hacia el aeropuerto para mayor comodidad.",
      features: [
        "Vehiculos cómodos",
        "Conductores profesionales",
        "Puntualidad garantizada",
        "Precios fijos"
      ],
      icon: "🚗"
    },
    {
      title: "Excursiones y Tours",
      description: "Descubre los mejores lugares con nuestros tours guiados y excursiones.",
      features: [
        "Guías expertos",
        "Grupos pequeños",
        "Experiencias únicas",
        "Precios accesibles"
      ],
      icon: "🗺️"
    },
    {
      title: "Consultoría de Viajes",
      description: "Asesoramiento personalizado para planificar el viaje perfecto según tus necesidades.",
      features: [
        "Planificación personalizada",
        "Itinerarios detallados",
        "Recomendaciones expertas",
        "Seguimiento continuo"
      ],
      icon: "💼"
    }
  ];

  return (
    <div className="min-h-screen bg-gray-50">
      {/* Hero Section */}
      <div className="hero">
        <div className="container">
          <div className="text-center">
            <h1 className="text-4xl md:text-6xl font-bold mb-4">
              Nuestros Servicios
            </h1>
            <p className="text-xl md:text-2xl">
              Todo lo que necesitas para tu viaje perfecto
            </p>
          </div>
        </div>
      </div>

      {/* Services Grid */}
      <div className="container py-16">
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-8">
          {services.map((service, index) => (
            <div key={index} className="card hover-lift">
              <div className="text-6xl mb-6 text-center">{service.icon}</div>
              <h3 className="text-2xl font-bold text-primary mb-4 text-center">
                {service.title}
              </h3>
              <p className="text-gray-600 mb-6 text-center">
                {service.description}
              </p>
              <ul className="feature-list">
                {service.features.map((feature, featureIndex) => (
                  <li key={featureIndex} className="text-gray-700">
                    {feature}
                  </li>
                ))}
              </ul>
            </div>
          ))}
        </div>
      </div>

      {/* Why Choose Us */}
      <div className="bg-white py-16">
        <div className="container">
          <div className="text-center mb-12">
            <h2 className="section-title">
              ¿Por qué elegir nuestros servicios?
            </h2>
            <p className="text-lg text-gray-600">
              Ofrecemos una experiencia completa y personalizada para cada viajero
            </p>
          </div>

          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-8">
            <div className="text-center hover-lift">
              <div className="w-16 h-16 airline-gradient-primary rounded-full flex items-center justify-center mx-auto mb-4">
                <svg className="w-8 h-8 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 8v4l3 3m6-3a9 9 0 11-18 0 9 9 0 0118 0z" />
                </svg>
              </div>
              <h3 className="text-xl font-bold text-primary mb-2">Rapidez</h3>
              <p className="text-gray-600">
                Procesos ágiles y reservas instantáneas
              </p>
            </div>

            <div className="text-center hover-lift">
              <div className="w-16 h-16 airline-gradient-sunset rounded-full flex items-center justify-center mx-auto mb-4">
                <svg className="w-8 h-8 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z" />
                </svg>
              </div>
              <h3 className="text-xl font-bold text-primary mb-2">Confiabilidad</h3>
              <p className="text-gray-600">
                Servicios verificados y garantizados
              </p>
            </div>

            <div className="text-center hover-lift">
              <div className="w-16 h-16 airline-gradient-sky rounded-full flex items-center justify-center mx-auto mb-4">
                <svg className="w-8 h-8 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 8c-1.657 0-3 .895-3 2s1.343 2 3 2 3 .895 3 2-1.343 2-3 2m0-8c1.11 0 2.08.402 2.599 1M12 8V7m0 1v8m0 0v1m0-1c-1.11 0-2.08-.402-2.599-1" />
                </svg>
              </div>
              <h3 className="text-xl font-bold text-primary mb-2">Precios Justos</h3>
              <p className="text-gray-600">
                Tarifas competitivas y transparentes
              </p>
            </div>

            <div className="text-center hover-lift">
              <div className="w-16 h-16 airline-gradient-primary rounded-full flex items-center justify-center mx-auto mb-4">
                <svg className="w-8 h-8 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M18.364 5.636l-3.536 3.536m0 5.656l3.536 3.536M9.172 9.172L5.636 5.636m3.536 9.192L5.636 18.364M12 2.25a9.75 9.75 0 100 19.5 9.75 9.75 0 000-19.5z" />
                </svg>
              </div>
              <h3 className="text-xl font-bold text-primary mb-2">Soporte 24/7</h3>
              <p className="text-gray-600">
                Atención disponible en todo momento
              </p>
            </div>
          </div>
        </div>
      </div>

      {/* CTA Section */}
      <div className="airline-gradient-primary py-16">
        <div className="container text-center">
          <h2 className="text-3xl font-bold text-white mb-4">
            ¿Listo para tu próximo viaje?
          </h2>
          <p className="text-xl text-white mb-8 opacity-90">
            Contáctanos y descubre cómo podemos hacer realidad tu viaje soñado
          </p>
          <div className="flex flex-col sm:flex-row gap-4 justify-center">
            <button className="btn btn-secondary">
              Consultar Servicios
            </button>
            <button className="btn btn-outline text-white border-white hover:bg-white hover:text-primary-600">
              Contactar Ahora
            </button>
          </div>
        </div>
      </div>
    </div>
  );
};

export default Services;
