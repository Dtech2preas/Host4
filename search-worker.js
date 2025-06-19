let allItems = [];

onmessage = function(e) {
  const { type, data } = e.data;

  if (type === "init") {
    allItems = data;
  } else if (type === "search") {
    const query = data.toLowerCase();
    const results = allItems.filter(item => 
      item.key.includes(query) || item.title.toLowerCase().includes(query)
    );
    postMessage(results);
  }
};