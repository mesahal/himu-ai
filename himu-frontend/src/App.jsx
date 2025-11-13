import { useState, useEffect, useRef } from "react";

function App() {
  // Store messages separately for each model
  const [modelMessages, setModelMessages] = useState({
    "himu-1": [],
    "himu-2": []
  });
  const [input, setInput] = useState("");
  const [isLoading, setIsLoading] = useState(false);
  const [selectedModel, setSelectedModel] = useState("himu-1");
  const messagesEndRef = useRef(null);

  // Get current model's messages
  const messages = modelMessages[selectedModel] || [];

  // Auto-scroll to bottom when messages change
  useEffect(() => {
    messagesEndRef.current?.scrollIntoView({ behavior: "smooth" });
  }, [messages, isLoading]);

  // Handle model change - switch to that model's chat history
  const handleModelChange = (newModel) => {
    setSelectedModel(newModel);
    // Clear any loading state when switching models
    setIsLoading(false);
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!input.trim()) return;

    const userMessage = { sender: "user", text: input };
    const currentModel = selectedModel;
    
    // Add user message to current model's history
    setModelMessages((prev) => ({
      ...prev,
      [currentModel]: [...prev[currentModel], userMessage]
    }));
    setInput("");

    setIsLoading(true);

    try {
      const response = await fetch("http://localhost:8080/api/chat", {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
        },
        body: JSON.stringify({ 
          message: input,
          model: currentModel
        }),
      });

      if (!response.ok) {
        throw new Error("Backend server error");
      }

      const data = await response.json();
      const himuMessage = { sender: "himu", text: data.reply };
      
      // Add response to current model's history
      setModelMessages((prev) => ({
        ...prev,
        [currentModel]: [...prev[currentModel], himuMessage]
      }));
    } catch (error) {
      const errorMessage = {
        sender: "himu",
        text: "আমার সাথে কথা বলতে সমস্যা হচ্ছে। পরে চেষ্টা করুন।",
      };
      
      // Add error message to current model's history
      setModelMessages((prev) => ({
        ...prev,
        [currentModel]: [...prev[currentModel], errorMessage]
      }));
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <div className="flex flex-col h-screen max-w-2xl mx-auto p-4">
      <div className="flex items-center justify-between mb-4">
        <div className="flex items-center gap-3">
          <h1 className="text-3xl font-bold text-yellow-400">
            হিমুর সাথে কথা বলুন
          </h1>
          <span className="px-3 py-1 text-sm bg-yellow-400/20 text-yellow-400 rounded-full border border-yellow-400/30">
            {selectedModel === "himu-1" ? "হিমু-১" : "হিমু-২"}
          </span>
        </div>
        <select
          value={selectedModel}
          onChange={(e) => handleModelChange(e.target.value)}
          disabled={isLoading}
          className="px-4 py-2 bg-gray-700 text-white border border-gray-600 rounded-lg focus:outline-none focus:ring-2 focus:ring-yellow-400 disabled:opacity-50 cursor-pointer transition-all hover:border-yellow-400/50"
        >
          <option value="himu-1">হিমু-১</option>
          <option value="himu-2">হিমু-২</option>
        </select>
      </div>

      {/* Chat Messages */}
      <div className="flex-1 overflow-y-auto space-y-4 p-4 bg-gray-800 rounded-lg shadow-inner">
        {messages.length === 0 && (
          <div className="text-center text-gray-400 mt-8">
            <p className="text-lg mb-2">
              {selectedModel === "himu-1" ? "হিমু-১" : "হিমু-২"} এর সাথে নতুন কথোপকথন শুরু করুন
            </p>
            <p className="text-sm">এখানে আপনার প্রশ্ন লিখুন...</p>
          </div>
        )}
        {messages.map((msg, index) => (
          <div
            key={index}
            className={`flex ${
              msg.sender === "user" ? "justify-end" : "justify-start"
            }`}
          >
            <div
              className={`p-3 rounded-lg max-w-xs md:max-w-md ${
                msg.sender === "user"
                  ? "bg-blue-600 text-white"
                  : "bg-gray-700 text-white"
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
        <div ref={messagesEndRef} />
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
