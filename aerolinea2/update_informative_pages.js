const fs = require('fs');
const path = require('path');

// Páginas a actualizar
const pages = [
  'baggage-info',
  'travel-tips', 
  'contact'
];

// Script template para cada página
const scriptTemplate = `import { ref, onMounted } from 'vue';
import { informativePagesApi } from '../../utils/informativePagesApi';

// State
const loading = ref(false);
const error = ref('');
const pageTitle = ref('{{TITLE}}');
const pageDescription = ref('{{DESCRIPTION}}');

// Methods
const loadPageContent = async () => {
  loading.value = true;
  error.value = '';
  
  try {
    const page = await informativePagesApi.getPageBySlug('{{SLUG}}');
    pageTitle.value = page.title;
    pageDescription.value = page.description;
  } catch (err) {
    console.error('Error loading page content:', err);
    // Keep default values if API fails
  } finally {
    loading.value = false;
  }
};

// Meta tags para SEO
onMounted(() => {
  loadPageContent();
  
  document.title = \`\${pageTitle.value} - AeroLinea\`;
  
  // Actualizar meta description
  const metaDescription = document.querySelector('meta[name="description"]');
  if (metaDescription) {
    metaDescription.setAttribute('content', pageDescription.value);
  }
});`;

// Títulos y descripciones por defecto
const pageData = {
  'baggage-info': {
    title: 'Información de Equipaje',
    description: 'Políticas y restricciones de equipaje para tu vuelo'
  },
  'travel-tips': {
    title: 'Consejos de Viaje',
    description: 'Consejos útiles para hacer tu viaje más placentero'
  },
  'contact': {
    title: 'Contacto',
    description: 'Información de contacto y soporte al cliente'
  }
};

// Función para actualizar una página
function updatePage(slug) {
  const filePath = path.join(__dirname, 'src', 'pages', 'informative', `${slug}.vue`);
  
  if (!fs.existsSync(filePath)) {
    console.log(`Archivo no encontrado: ${filePath}`);
    return;
  }
  
  let content = fs.readFileSync(filePath, 'utf8');
  
  // Reemplazar el script section
  const scriptRegex = /<script setup lang="ts">[\s\S]*?<\/script>/;
  const newScript = scriptTemplate
    .replace('{{TITLE}}', pageData[slug].title)
    .replace('{{DESCRIPTION}}', pageData[slug].description)
    .replace('{{SLUG}}', slug);
  
  content = content.replace(scriptRegex, `<script setup lang="ts">\n${newScript}\n</script>`);
  
  // Reemplazar el título y descripción en el template
  const titleRegex = /<h1[^>]*>.*?<\/h1>/;
  const descRegex = /<p[^>]*class="[^"]*text-blue-100[^"]*"[^>]*>.*?<\/p>/;
  
  content = content.replace(titleRegex, `<h1 class="text-4xl md:text-5xl font-bold mb-4 animate-fade-in-up">{{ pageTitle }}</h1>`);
  content = content.replace(descRegex, `<p class="text-xl md:text-2xl text-blue-100 animate-fade-in-up" style="animation-delay: 0.2s;">\n          {{ pageDescription }}\n        </p>`);
  
  fs.writeFileSync(filePath, content);
  console.log(`Actualizado: ${slug}.vue`);
}

// Actualizar todas las páginas
pages.forEach(updatePage);

console.log('¡Todas las páginas han sido actualizadas!');

