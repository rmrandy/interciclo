<!-- eslint-disable vue/multi-word-component-names -->
<template>
    <footer class="bg-blue-600 text-white text-center p-4">
        <div class="footer-content">
            <p class="footer-text" @click="editFooterText" v-if="!isEditingText">
                {{ footerText }}
            </p>
            <input 
                v-if="isEditingText" 
                v-model="editingText" 
                @blur="saveFooterText" 
                @keyup.enter="saveFooterText"
                @keyup.esc="cancelEditText"
                class="edit-footer-input"
                ref="footerTextInput"
            />
            <span class="edit-icon" v-if="isAdmin && !isEditingText" @click="editFooterText">✏️</span>
            
            <p class="footer-contact" @click="editFooterContact" v-if="!isEditingContact">
                {{ footerContact }}
            </p>
            <input 
                v-if="isEditingContact" 
                v-model="editingContact" 
                @blur="saveFooterContact" 
                @keyup.enter="saveFooterContact"
                @keyup.esc="cancelEditContact"
                class="edit-footer-input"
                ref="footerContactInput"
            />
            <span class="edit-icon" v-if="isAdmin && !isEditingContact" @click="editFooterContact">✏️</span>
        </div>
    </footer>
</template>

<script>
import { ref, computed, onMounted } from 'vue';
import { useUserStore } from '@/stores/userStore';
import SiteContentService from '@/services/SiteContentService';
import { getPharmacyApiUrl } from '@/services/ApiService';

export default {
    name: 'AppFooter',
    
    setup() {
        const userStore = useUserStore();
        
        // Contenido dinámico
        const footerText = ref('');
        const footerContact = ref('');
        const isEditingText = ref(false);
        const isEditingContact = ref(false);
        const editingText = ref('');
        const editingContact = ref('');
        const footerTextInput = ref(null);
        const footerContactInput = ref(null);
        
        const isAdmin = computed(() => {
            const user = userStore.getUser();
            return user && (user.role === 'admin' || user.role === 'administrador');
        });
        
        // Funciones para editar texto del footer
        const editFooterText = () => {
            if (!isAdmin.value) return;
            
            editingText.value = footerText.value;
            isEditingText.value = true;
            
            setTimeout(() => {
                if (footerTextInput.value) {
                    footerTextInput.value.focus();
                    footerTextInput.value.select();
                }
            }, 0);
        };
        
        const saveFooterText = async () => {
            try {
                await SiteContentService.updateContent('footer_text', editingText.value);
                footerText.value = editingText.value;
                isEditingText.value = false;
            } catch (error) {
                console.error('Error al guardar el texto del footer:', error);
                alert('Error al guardar el texto del footer');
            }
        };
        
        const cancelEditText = () => {
            isEditingText.value = false;
            editingText.value = footerText.value;
        };
        
        // Funciones para editar contacto del footer
        const editFooterContact = () => {
            if (!isAdmin.value) return;
            
            editingContact.value = footerContact.value;
            isEditingContact.value = true;
            
            setTimeout(() => {
                if (footerContactInput.value) {
                    footerContactInput.value.focus();
                    footerContactInput.value.select();
                }
            }, 0);
        };
        
        const saveFooterContact = async () => {
            try {
                await SiteContentService.updateContent('footer_contact', editingContact.value);
                footerContact.value = editingContact.value;
                isEditingContact.value = false;
            } catch (error) {
                console.error('Error al guardar el contacto del footer:', error);
                alert('Error al guardar el contacto del footer');
            }
        };
        
        const cancelEditContact = () => {
            isEditingContact.value = false;
            editingContact.value = footerContact.value;
        };
        
        // Cargar contenido del backend al montar
        onMounted(async () => {
            try {
                const res = await fetch(getPharmacyApiUrl('site-content-v2'));
                const data = await res.json();
                const foundText = data.find(item => item.key === "footer_text");
                const foundContact = data.find(item => item.key === "footer_contact");
                footerText.value = foundText && foundText.value ? foundText.value : '';
                footerContact.value = foundContact && foundContact.value ? foundContact.value : '';
            } catch (e) {
                footerText.value = '';
                footerContact.value = '';
            }
        });
        
        return {
            footerText,
            footerContact,
            isEditingText,
            isEditingContact,
            editingText,
            editingContact,
            footerTextInput,
            footerContactInput,
            isAdmin,
            editFooterText,
            saveFooterText,
            cancelEditText,
            editFooterContact,
            saveFooterContact,
            cancelEditContact
        };
    }
}
</script>

<style scoped>
footer {
    background-color: #2563eb;
    font-size: 14px;
    font-weight: bold;
}

.footer-content {
    position: relative;
    max-width: 1200px;
    margin: 0 auto;
}

.footer-text, .footer-contact {
    cursor: pointer;
    transition: all 0.3s ease;
    padding: 4px 8px;
    border-radius: 6px;
    margin: 4px 0;
    display: inline-block;
}

.footer-text:hover, .footer-contact:hover {
    background: rgba(255, 255, 255, 0.1);
}

.edit-footer-input {
    background: rgba(255, 255, 255, 0.95);
    border: 2px solid #2563eb;
    border-radius: 6px;
    padding: 4px 8px;
    font-size: 14px;
    font-weight: bold;
    color: #2563eb;
    outline: none;
    min-width: 300px;
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.2);
    margin: 4px 0;
}

.edit-footer-input:focus {
    border-color: #10b981;
    box-shadow: 0 4px 12px rgba(16, 185, 129, 0.3);
}

.edit-icon {
    position: absolute;
    top: 0;
    right: 20px;
    background: rgba(255, 255, 255, 0.9);
    border-radius: 50%;
    width: 20px;
    height: 20px;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 10px;
    cursor: pointer;
    opacity: 0;
    transition: all 0.3s ease;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.2);
}

.footer-content:hover .edit-icon {
    opacity: 1;
}

.edit-icon:hover {
    background: white;
    transform: scale(1.1);
}
</style>