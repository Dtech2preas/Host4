const worker = new Worker('search-worker.js');
const searchInput = document.getElementById("search");

let clusterize = new Clusterize({
  rows: [],
  scrollId: 'scrollArea',
  contentId: 'contentArea'
});

let allItems = [];

fetch('vid.json')
  .then(res => res.json())
  .then(data => {
    for (const key in data) {
      data[key].forEach(item => {
        allItems.push({
          key,
          title: item.title,
          link: item.link
        });
      });
    }

    worker.postMessage({ type: "init", data: allItems });
    renderItems(allItems.slice(0, 100));
  });

function renderItems(items) {
  const rows = items.map(item => 
    `<li><strong>${item.key}</strong>: <a href="${item.link}" target="_blank">${item.title}</a></li>`
  );
  clusterize.update(rows);
}

searchInput.addEventListener("input", () => {
  const query = searchInput.value.trim();
  if (query.length < 2) {
    renderItems(allItems.slice(0, 100));
    return;
  }
  worker.postMessage({ type: "search", data: query });
});

worker.onmessage = function(e) {
  renderItems(e.data);
};