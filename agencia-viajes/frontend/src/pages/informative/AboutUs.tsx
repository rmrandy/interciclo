import React from 'react';

const AboutUs: React.FC = () => {
  return (
    <div className="min-h-screen bg-gray-50">
      {/* Hero Section */}
      <div className="hero">
        <div className="container">
          <div className="text-center">
            <h1 className="text-4xl md:text-6xl font-bold mb-4">
              Sobre Nosotros
            </h1>
            <p className="text-xl md:text-2xl">
              Tu agencia de confianza para viajar por el mundo
            </p>
          </div>
        </div>
      </div>

      {/* Main Content */}
      <div className="container py-16">
        <div className="grid grid-cols-1 lg:grid-cols-2 gap-12 items-center mb-16">
          <div className="animate-fade-in-left">
            <h2 className="section-title text-left mb-6">
              Nuestra Historia
            </h2>
            <p className="text-lg text-gray-600 mb-4">
              Con más de 15 años de experiencia en el sector turístico, hemos ayudado a miles de viajeros 
              a cumplir sus sueños de conocer el mundo. Nuestra pasión por los viajes y nuestro compromiso 
              con la excelencia nos han convertido en una de las agencias de viajes más confiables del país.
            </p>
            <p className="text-lg text-gray-600">
              Desde nuestros inicios, hemos creído que viajar es una de las experiencias más enriquecedoras 
              que puede tener una persona. Por eso, trabajamos incansablemente para ofrecerte las mejores 
              opciones de vuelos, hoteles y experiencias únicas.
            </p>
          </div>
          <div className="card-premium animate-fade-in-right">
            <div className="grid grid-cols-2 gap-6 text-center">
              <div className="hover-lift">
                <div className="text-4xl font-bold text-primary mb-2">15+</div>
                <div className="text-gray-600 font-medium">Años de experiencia</div>
              </div>
              <div className="hover-lift">
                <div className="text-4xl font-bold text-primary mb-2">50K+</div>
                <div className="text-gray-600 font-medium">Clientes satisfechos</div>
              </div>
              <div className="hover-lift">
                <div className="text-4xl font-bold text-primary mb-2">100+</div>
                <div className="text-gray-600 font-medium">Destinos disponibles</div>
              </div>
              <div className="hover-lift">
                <div className="text-4xl font-bold text-primary mb-2">24/7</div>
                <div className="text-gray-600 font-medium">Atención al cliente</div>
              </div>
            </div>
          </div>
        </div>

        {/* Mission and Vision */}
        <div className="grid grid-cols-1 md:grid-cols-2 gap-8 mb-16">
          <div className="card hover-lift">
            <div className="w-16 h-16 airline-gradient-primary rounded-full flex items-center justify-center mb-6">
              <svg className="w-8 h-8 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M13 10V3L4 14h7v7l9-11h-7z" />
              </svg>
            </div>
            <h3 className="text-2xl font-bold text-primary mb-4">Nuestra Misión</h3>
            <p className="text-gray-600">
              Facilitar experiencias de viaje excepcionales, conectando a las personas con destinos 
              increíbles a través de un servicio personalizado, precios competitivos y la más alta 
              calidad en cada detalle de su viaje.
            </p>
          </div>

          <div className="card hover-lift">
            <div className="w-16 h-16 airline-gradient-sunset rounded-full flex items-center justify-center mb-6">
              <svg className="w-8 h-8 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M15 12a3 3 0 11-6 0 3 3 0 016 0z" />
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M2.458 12C3.732 7.943 7.523 5 12 5c4.478 0 8.268 2.943 9.542 7-1.274 4.057-5.064 7-9.542 7-4.477 0-8.268-2.943-9.542-7z" />
              </svg>
            </div>
            <h3 className="text-2xl font-bold text-primary mb-4">Nuestra Visión</h3>
            <p className="text-gray-600">
              Ser la agencia de viajes líder en innovación y satisfacción del cliente, reconocida 
              por hacer realidad los sueños de viaje de cada persona y crear conexiones significativas 
              entre culturas y destinos alrededor del mundo.
            </p>
          </div>
        </div>

        {/* Values */}
        <div className="mb-16">
          <h2 className="text-3xl font-bold text-gray-900 text-center mb-12">
            Nuestros Valores
          </h2>
          <div className="grid grid-cols-1 md:grid-cols-3 gap-8">
            <div className="text-center">
              <div className="w-20 h-20 bg-primary-100 rounded-full flex items-center justify-center mx-auto mb-4">
                <svg className="w-10 h-10 text-primary-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z" />
                </svg>
              </div>
              <h3 className="text-xl font-semibold text-gray-900 mb-2">Confianza</h3>
              <p className="text-gray-600">
                Construimos relaciones duraderas basadas en la transparencia y la honestidad.
              </p>
            </div>

            <div className="text-center">
              <div className="w-20 h-20 bg-primary-100 rounded-full flex items-center justify-center mx-auto mb-4">
                <svg className="w-10 h-10 text-primary-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M13 10V3L4 14h7v7l9-11h-7z" />
                </svg>
              </div>
              <h3 className="text-xl font-semibold text-gray-900 mb-2">Innovación</h3>
              <p className="text-gray-600">
                Utilizamos tecnología de vanguardia para mejorar tu experiencia de viaje.
              </p>
            </div>

            <div className="text-center">
              <div className="w-20 h-20 bg-primary-100 rounded-full flex items-center justify-center mx-auto mb-4">
                <svg className="w-10 h-10 text-primary-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M4.318 6.318a4.5 4.5 0 000 6.364L12 20.364l7.682-7.682a4.5 4.5 0 00-6.364-6.364L12 7.636l-1.318-1.318a4.5 4.5 0 00-6.364 0z" />
                </svg>
              </div>
              <h3 className="text-xl font-semibold text-gray-900 mb-2">Pasión</h3>
              <p className="text-gray-600">
                Amamos los viajes y trabajamos con pasión para hacer realidad tus sueños.
              </p>
            </div>
          </div>
        </div>

        {/* Team */}
        <div className="bg-white rounded-lg shadow-lg p-8">
          <h2 className="text-3xl font-bold text-gray-900 text-center mb-12">
            Nuestro Equipo
          </h2>
          <div className="grid grid-cols-1 md:grid-cols-3 gap-8">
            <div className="text-center">
              <div className="w-32 h-32 bg-primary-200 rounded-full mx-auto mb-4 flex items-center justify-center">
                <span className="text-4xl font-bold text-primary-600">MR</span>
              </div>
              <h3 className="text-xl font-semibold text-gray-900 mb-2">María Rodríguez</h3>
              <p className="text-primary-600 mb-2">Directora General</p>
              <p className="text-gray-600 text-sm">
                Con más de 20 años en la industria turística, María lidera nuestro equipo 
                con visión estratégica y pasión por los viajes.
              </p>
            </div>

            <div className="text-center">
              <div className="w-32 h-32 bg-primary-200 rounded-full mx-auto mb-4 flex items-center justify-center">
                <span className="text-4xl font-bold text-primary-600">CP</span>
              </div>
              <h3 className="text-xl font-semibold text-gray-900 mb-2">Carlos Pérez</h3>
              <p className="text-primary-600 mb-2">Gerente de Operaciones</p>
              <p className="text-gray-600 text-sm">
                Especialista en logística y operaciones, Carlos asegura que cada viaje 
                sea perfecto desde el inicio hasta el final.
              </p>
            </div>

            <div className="text-center">
              <div className="w-32 h-32 bg-primary-200 rounded-full mx-auto mb-4 flex items-center justify-center">
                <span className="text-4xl font-bold text-primary-600">AG</span>
              </div>
              <h3 className="text-xl font-semibold text-gray-900 mb-2">Ana García</h3>
              <p className="text-primary-600 mb-2">Directora de Atención al Cliente</p>
              <p className="text-gray-600 text-sm">
                Ana y su equipo están disponibles 24/7 para resolver cualquier consulta 
                y asegurar tu satisfacción total.
              </p>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};

export default AboutUs;
