import { getAirlineApiUrl } from './api'

export interface InformativePage {
  id: number
  slug: string
  title: string
  description: string
  content: string
  is_active: boolean
  created_at: string
  updated_at: string
}

export interface CreateInformativePageRequest {
  slug: string
  title: string
  description: string
  content: string
  is_active?: boolean
}

export interface UpdateInformativePageRequest {
  title?: string
  description?: string
  content?: string
  is_active?: boolean
}

class InformativePagesApiClient {
  private baseUrl: string

  constructor() {
    this.baseUrl = `${getAirlineApiUrl()}/admin/informative-pages`
  }

  async getAllPages(): Promise<InformativePage[]> {
    try {
      const response = await fetch(`${this.baseUrl}`, {
        method: 'GET',
        headers: {
          'Content-Type': 'application/json',
          'Authorization': `Bearer ${localStorage.getItem('token') || ''}`
        }
      })

      if (!response.ok) {
        throw new Error(`Error ${response.status}: ${response.statusText}`)
      }

      const data = await response.json()
      return data.pages || []
    } catch (error) {
      console.error('Error fetching informative pages:', error)
      throw error
    }
  }

  async getPageById(id: number): Promise<InformativePage> {
    try {
      const response = await fetch(`${this.baseUrl}/${id}`, {
        method: 'GET',
        headers: {
          'Content-Type': 'application/json',
          'Authorization': `Bearer ${localStorage.getItem('token') || ''}`
        }
      })

      if (!response.ok) {
        throw new Error(`Error ${response.status}: ${response.statusText}`)
      }

      const data = await response.json()
      return data.page
    } catch (error) {
      console.error('Error fetching informative page:', error)
      throw error
    }
  }

  async getPageBySlug(slug: string): Promise<InformativePage> {
    try {
      const response = await fetch(`${this.baseUrl}/slug/${slug}`, {
        method: 'GET',
        headers: {
          'Content-Type': 'application/json'
        }
      })

      if (!response.ok) {
        throw new Error(`Error ${response.status}: ${response.statusText}`)
      }

      const data = await response.json()
      return data.page
    } catch (error) {
      console.error('Error fetching informative page by slug:', error)
      throw error
    }
  }

  async createPage(pageData: CreateInformativePageRequest): Promise<InformativePage> {
    try {
      const response = await fetch(`${this.baseUrl}`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          'Authorization': `Bearer ${localStorage.getItem('token') || ''}`
        },
        body: JSON.stringify(pageData)
      })

      if (!response.ok) {
        throw new Error(`Error ${response.status}: ${response.statusText}`)
      }

      const data = await response.json()
      return data.page
    } catch (error) {
      console.error('Error creating informative page:', error)
      throw error
    }
  }

  async updatePage(id: number, pageData: UpdateInformativePageRequest): Promise<InformativePage> {
    try {
      const response = await fetch(`${this.baseUrl}/${id}`, {
        method: 'PUT',
        headers: {
          'Content-Type': 'application/json',
          'Authorization': `Bearer ${localStorage.getItem('token') || ''}`
        },
        body: JSON.stringify(pageData)
      })

      if (!response.ok) {
        throw new Error(`Error ${response.status}: ${response.statusText}`)
      }

      const data = await response.json()
      return data.page
    } catch (error) {
      console.error('Error updating informative page:', error)
      throw error
    }
  }

  async deletePage(id: number): Promise<void> {
    try {
      const response = await fetch(`${this.baseUrl}/${id}`, {
        method: 'DELETE',
        headers: {
          'Content-Type': 'application/json',
          'Authorization': `Bearer ${localStorage.getItem('token') || ''}`
        }
      })

      if (!response.ok) {
        throw new Error(`Error ${response.status}: ${response.statusText}`)
      }
    } catch (error) {
      console.error('Error deleting informative page:', error)
      throw error
    }
  }

  async togglePageStatus(id: number): Promise<InformativePage> {
    try {
      const response = await fetch(`${this.baseUrl}/${id}/toggle-status`, {
        method: 'PATCH',
        headers: {
          'Content-Type': 'application/json',
          'Authorization': `Bearer ${localStorage.getItem('token') || ''}`
        }
      })

      if (!response.ok) {
        throw new Error(`Error ${response.status}: ${response.statusText}`)
      }

      const data = await response.json()
      return data.page
    } catch (error) {
      console.error('Error toggling page status:', error)
      throw error
    }
  }
}

export const informativePagesApi = new InformativePagesApiClient()

