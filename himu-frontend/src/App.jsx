import { useState } from 'react';

function App() {
  const [messages, setMessages] = useState([]);
  const [input, setInput] = useState('');
  const [isLoading, setIsLoading] = useState(false);

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!input.trim()) return;

    const userMessage = { sender: 'user', text: input };
    setMessages((prev) => [...prev, userMessage]);
    setInput('');

    setIsLoading(true);

    try {
      const response = await fetch('http://localhost:8080/api/chat', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({ message: input }),
      });

      if (!response.ok) {
        throw new Error('Backend server error');
      }

      const data = await response.json();
      const himuMessage = { sender: 'himu', text: data.reply };
      setMessages((prev) => [...prev, himuMessage]);
    } catch (error) {
      const errorMessage = { sender: 'himu', text: 'আমার সাথে কথা বলতে সমস্যা হচ্ছে। পরে চেষ্টা করুন।' };
      setMessages((prev) => [...prev, errorMessage]);
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <div className="flex flex-col h-screen max-w-2xl mx-auto p-4">
      <h1 className="text-3xl font-bold text-center text-yellow-400 p-4">হিমুর সাথে কথা বলুন</h1>
      
      {/* Chat Messages */}
      <div className="flex-1 overflow-y-auto space-y-4 p-4 bg-gray-800 rounded-lg shadow-inner">
        {messages.length === 0 && (
          <div className="text-center text-gray-400 mt-8">
            <p>হিমুকে আপনার কোনো প্রশ্ন করুন...</p>
          </div>
        )}
        {messages.map((msg, index) => (
          <div key={index} className={`flex ${msg.sender === 'user' ? 'justify-end' : 'justify-start'}`}>
            <div
              className={`p-3 rounded-lg max-w-xs md:max-w-md ${
                msg.sender === 'user' ? 'bg-blue-600 text-white' : 'bg-gray-700 text-white'
              }`}
            >
              {msg.text}
            </div>
          </div>
        ))}
        {isLoading && (
          <div className="flex justify-start">
            <div className="p-3 rounded-lg bg-gray-700 text-white">
              <span className="animate-pulse">হিমু ভাবছে...</span>
            </div>
          </div>
        )}
      </div>

      {/* Input Form */}
      <form onSubmit={handleSubmit} className="flex mt-4">
        <input
          type="text"
          value={input}
          onChange={(e) => setInput(e.target.value)}
          placeholder="এখানে আপনার কথা লিখুন..."
          className="flex-1 p-3 bg-gray-700 text-white border border-gray-600 rounded-l-lg focus:outline-none focus:ring-2 focus:ring-yellow-400"
          disabled={isLoading}
        />
        <button
          type="submit"
          className="p-3 bg-yellow-400 text-gray-900 font-bold rounded-r-lg hover:bg-yellow-500 disabled:opacity-50"
          disabled={isLoading}
        >
          পাঠান
        </button>
      </form>
    </div>
  );
}

export default App;

