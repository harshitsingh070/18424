import { useState, useEffect } from 'react'

const API_BASE = 'http://localhost:8080/api/notifications'

function App() {
  const [notifications, setNotifications] = useState([])
  const [selectedType, setSelectedType] = useState('All')
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState(null)

  const fetchNotifications = async () => {
    setLoading(true)
    setError(null)
    try {
      const url = selectedType === 'All' 
        ? `${API_BASE}` 
        : `${API_BASE}?type=${selectedType}`
      const response = await fetch(url)
      const data = await response.json()
      setNotifications(data)
    } catch (err) {
      setError('Failed to load notifications')
    }
    setLoading(false)
  }


  const syncNotifications = async () => {
    setLoading(true)
    try {
      await fetch(`${API_BASE}/sync`, { method: 'POST' })
      await fetchNotifications()
    } catch (err) {
      setError('Failed to sync')
    }
    setLoading(false)
  }
  const markAsRead = async (id) => {
    try {
      await fetch(`${API_BASE}/${id}/read`, { method: 'PATCH' })
      await fetchNotifications()
    } catch (err) {
      setError('Failed to update')
    }
  }

  const deleteNotification = async (id) => {
    try {
      await fetch(`${API_BASE}/${id}`, { method: 'DELETE' })
      await fetchNotifications()
    } catch (err) {
      setError('Failed to delete')
    }
  }

 
  useEffect(() => {
    fetchNotifications()
  }, [])


  useEffect(() => {
    fetchNotifications()
  }, [selectedType])

  return (
    <div className="min-h-screen bg-gray-50">
      <div className="max-w-3xl mx-auto px-4 py-8">
        {/* Header */}
        <div className="flex justify-between items-center mb-8 pb-6 border-b-2 border-gray-200">
          <h1 className="text-4xl font-bold text-gray-900">Notifications</h1>
          <button 
            onClick={syncNotifications}
            disabled={loading}
            className="px-6 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 disabled:bg-gray-500 disabled:cursor-not-allowed transition-colors"
          >
            {loading ? 'Loading...' : 'Refresh'}
          </button>
        </div>

        { }
        {error && (
          <div className="mb-6 p-4 bg-red-100 text-red-700 rounded-lg border border-red-300">
            {error}
          </div>
        )}

        {}
        <div className="mb-8">
          <h3 className="text-lg font-semibold text-gray-700 mb-3">Filter by type:</h3>
          <div className="flex gap-3 flex-wrap">
            {['All', 'Placement', 'Result', 'Event'].map(type => (
              <button 
                key={type}
                onClick={() => setSelectedType(type)}
                className={`px-4 py-2 rounded-lg transition-all ${
                  selectedType === type
                    ? 'bg-blue-600 text-white'
                    : 'bg-gray-200 text-gray-700 hover:bg-gray-300'
                }`}
              >
                {type}
              </button>
            ))}
          </div>
        </div>

        {}
        <div className="space-y-4">
          {notifications.length === 0 ? (
            <div className="text-center py-16 text-gray-500">
              <p className="text-lg">No notifications found</p>
            </div>
          ) : (
            notifications.map(notif => (
              <div 
                key={notif.id}
                className={`p-4 rounded-lg border-2 shadow-sm hover:shadow-md transition-shadow ${
                  notif.read
                    ? 'bg-white border-gray-200'
                    : 'bg-blue-50 border-l-4 border-l-blue-600 border-gray-200'
                }`}
              >
                {}
                <div className="flex justify-between items-start mb-3">
                  <h3 className="text-lg font-semibold text-gray-900 flex-1">{notif.title}</h3>
                  <span className={`ml-3 px-3 py-1 rounded-full text-xs font-bold text-white ${
                    notif.type === 'Placement' ? 'bg-green-500' :
                    notif.type === 'Result' ? 'bg-yellow-500' :
                    'bg-cyan-500'
                  }`}>
                    {notif.type}
                  </span>
                </div>

                {}
                <p className="text-gray-700 mb-3 leading-relaxed">{notif.message}</p>

                {/* Meta Info */}
                <div className="flex gap-4 mb-4 text-sm text-gray-500">
                  <span>{notif.timestamp}</span>
                  <span className="font-medium text-gray-700">Priority: {notif.priority}</span>
                </div>

                {/* Actions */}
                <div className="flex gap-2">
                  {!notif.read && (
                    <button 
                      onClick={() => markAsRead(notif.id)}
                      className="px-4 py-2 bg-green-600 text-white rounded hover:bg-green-700 text-sm transition-colors"
                    >
                      Mark as Read
                    </button>
                  )}
                  <button 
                    onClick={() => deleteNotification(notif.id)}
                    className="px-4 py-2 bg-red-600 text-white rounded hover:bg-red-700 text-sm transition-colors"
                  >
                    Delete
                  </button>
                </div>
              </div>
            ))
          )}
        </div>
      </div>
    </div>
  )
}

export default App
