<template>
  <div class="product-gallery">
    <!-- Imagen principal -->
    <div class="main-image-container">
      <img
        v-if="currentImage"
        :src="currentImage"
        :alt="`${productName} - Imagen ${currentImageIndex + 1}`"
        class="main-image"
        @click="openLightbox"
      />
      <div v-else class="placeholder-image">
        <i class="fas fa-prescription-bottle-alt"></i>
        <p>Sin imagen</p>
      </div>
    </div>

    <!-- Miniaturas de navegación -->
    <div v-if="images.length > 1" class="thumbnails-container">
      <div class="thumbnails">
        <div
          v-for="(image, index) in images"
          :key="index"
          class="thumbnail"
          :class="{ active: index === currentImageIndex }"
          @click="selectImage(index)"
        >
          <img
            :src="image"
            :alt="`Miniatura ${index + 1}`"
            class="thumbnail-img"
          />
        </div>
      </div>
      
      <!-- Controles de navegación -->
      <div class="gallery-controls">
        <button 
          @click="previousImage" 
          :disabled="currentImageIndex === 0"
          class="nav-btn prev-btn"
        >
          ‹
        </button>
        <span class="image-counter">{{ currentImageIndex + 1 }} / {{ images.length }}</span>
        <button 
          @click="nextImage" 
          :disabled="currentImageIndex === images.length - 1"
          class="nav-btn next-btn"
        >
          ›
        </button>
      </div>
    </div>

    <!-- Lightbox para vista ampliada -->
    <div v-if="showLightbox" class="lightbox-overlay" @click="closeLightbox">
      <div class="lightbox-content" @click.stop>
        <button class="lightbox-close" @click="closeLightbox">×</button>
        <img
          :src="currentImage"
          :alt="`${productName} - Imagen ${currentImageIndex + 1}`"
          class="lightbox-image"
        />
        <div class="lightbox-controls">
          <button 
            @click="previousImage" 
            :disabled="currentImageIndex === 0"
            class="lightbox-nav-btn"
          >
            ‹
          </button>
          <span class="lightbox-counter">{{ currentImageIndex + 1 }} / {{ images.length }}</span>
          <button 
            @click="nextImage" 
            :disabled="currentImageIndex === images.length - 1"
            class="lightbox-nav-btn"
          >
            ›
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'ProductImageGallery',
  props: {
    images: {
      type: Array,
      default: () => []
    },
    productName: {
      type: String,
      default: 'Producto'
    }
  },
  data() {
    return {
      currentImageIndex: 0,
      showLightbox: false
    };
  },
  computed: {
    currentImage() {
      return this.images[this.currentImageIndex] || null;
    }
  },
  methods: {
    selectImage(index) {
      this.currentImageIndex = index;
    },
    nextImage() {
      if (this.currentImageIndex < this.images.length - 1) {
        this.currentImageIndex++;
      }
    },
    previousImage() {
      if (this.currentImageIndex > 0) {
        this.currentImageIndex--;
      }
    },
    openLightbox() {
      this.showLightbox = true;
      document.body.style.overflow = 'hidden';
    },
    closeLightbox() {
      this.showLightbox = false;
      document.body.style.overflow = 'auto';
    }
  },
  mounted() {
    // Manejar navegación con teclado
    const handleKeydown = (e) => {
      if (!this.showLightbox) return;
      
      switch (e.key) {
        case 'ArrowLeft':
          this.previousImage();
          break;
        case 'ArrowRight':
          this.nextImage();
          break;
        case 'Escape':
          this.closeLightbox();
          break;
      }
    };
    
    document.addEventListener('keydown', handleKeydown);
    
    // Limpiar el event listener cuando el componente se destruya
    this.$nextTick(() => {
      this.$options._cleanup = () => {
        document.removeEventListener('keydown', handleKeydown);
      };
    });
  },
  beforeUnmount() {
    // Limpiar el event listener
    if (this.$options._cleanup) {
      this.$options._cleanup();
    }
  }
};
</script>

<style scoped>
.product-gallery {
  position: relative;
}

.main-image-container {
  width: 100%;
  height: 400px;
  border-radius: 12px;
  overflow: hidden;
  background: #f8fafc;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: transform 0.2s ease;
}

.main-image-container:hover {
  transform: scale(1.02);
}

.main-image {
  width: 100%;
  height: 100%;
  object-fit: contain;
  transition: transform 0.3s ease;
}

.placeholder-image {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: #94a3b8;
  font-size: 3rem;
}

.placeholder-image p {
  margin-top: 1rem;
  font-size: 1rem;
  color: #64748b;
}

.thumbnails-container {
  margin-top: 1rem;
}

.thumbnails {
  display: flex;
  gap: 0.5rem;
  margin-bottom: 1rem;
  overflow-x: auto;
  padding-bottom: 0.5rem;
}

.thumbnail {
  flex-shrink: 0;
  width: 80px;
  height: 80px;
  border-radius: 8px;
  overflow: hidden;
  cursor: pointer;
  border: 2px solid transparent;
  transition: all 0.2s ease;
}

.thumbnail.active {
  border-color: #3b82f6;
  transform: scale(1.05);
}

.thumbnail:hover {
  border-color: #60a5fa;
}

.thumbnail-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.gallery-controls {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 1rem;
}

.nav-btn {
  width: 40px;
  height: 40px;
  border: none;
  border-radius: 50%;
  background: #3b82f6;
  color: white;
  font-size: 1.5rem;
  cursor: pointer;
  transition: all 0.2s ease;
  display: flex;
  align-items: center;
  justify-content: center;
}

.nav-btn:hover:not(:disabled) {
  background: #2563eb;
  transform: scale(1.1);
}

.nav-btn:disabled {
  background: #cbd5e1;
  cursor: not-allowed;
}

.image-counter {
  font-size: 0.9rem;
  color: #64748b;
  font-weight: 500;
}

/* Lightbox */
.lightbox-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.9);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 9999;
}

.lightbox-content {
  position: relative;
  max-width: 90vw;
  max-height: 90vh;
}

.lightbox-close {
  position: absolute;
  top: -50px;
  right: 0;
  background: none;
  border: none;
  color: white;
  font-size: 2rem;
  cursor: pointer;
  z-index: 10000;
}

.lightbox-image {
  max-width: 100%;
  max-height: 80vh;
  object-fit: contain;
  border-radius: 8px;
}

.lightbox-controls {
  position: absolute;
  bottom: -60px;
  left: 50%;
  transform: translateX(-50%);
  display: flex;
  align-items: center;
  gap: 1rem;
}

.lightbox-nav-btn {
  width: 50px;
  height: 50px;
  border: none;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.2);
  color: white;
  font-size: 2rem;
  cursor: pointer;
  transition: all 0.2s ease;
  display: flex;
  align-items: center;
  justify-content: center;
}

.lightbox-nav-btn:hover:not(:disabled) {
  background: rgba(255, 255, 255, 0.3);
}

.lightbox-nav-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.lightbox-counter {
  color: white;
  font-size: 1rem;
  font-weight: 500;
}

/* Responsive */
@media (max-width: 768px) {
  .main-image-container {
    height: 300px;
  }
  
  .thumbnail {
    width: 60px;
    height: 60px;
  }
  
  .nav-btn {
    width: 35px;
    height: 35px;
    font-size: 1.2rem;
  }
}
</style> 