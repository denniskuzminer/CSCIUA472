import { captureConsoleOutput } from "./logger.mjs";

const data = {
  A: [1, 10],
  B: [10, 10],
  C: [1, 9.1],
  D: [5, 0],
  E: [4.5, 1],
  F: [9.1, 8.9],
  G: [2, 8.5],
};

const d = (arr1, arr2, data) => {
  let maxDistance = 0;

  for (let i = 0; i < arr1.length; i++) {
    for (let j = 0; j < arr2.length; j++) {
      const distance = Math.sqrt(
        (data[arr1[i]][0] - data[arr2[j]][0]) ** 2 +
          (data[arr1[i]][1] - data[arr2[j]][1]) ** 2
      );
      if (distance > maxDistance) {
        maxDistance = distance;
      }
    }
  }

  return maxDistance;
};

const findNextJoin = (H, data) => {
  let minimal = null;
  let smallestValue = Number.MAX_VALUE;

  for (let i = 0; i < H.length; i++) {
    for (let j = i + 1; j < H.length; j++) {
      const value = d(H[i], H[j], data);
      if (value < smallestValue) {
        minimal = [H[i], H[j]];
        smallestValue = value;
      }
    }
  }

  return minimal;
};

const agglomerative = (T) => {
  const mergeTree = [];
  let currStep = 0;
  let H = Object.entries(T).map(([k, v], i) => {
    mergeTree.push({ currStep, clusters: [k] });
    return [k];
  });
  for (let i = 0; i < Object.entries(T).length - 1; i++) {
    const [C1, C2] = findNextJoin(H, T);
    const C = [...C1, ...C2];
    H.splice(H.indexOf(C1), 1);
    H.splice(H.indexOf(C2), 1);
    H.push(C);
    currStep++;
    mergeTree.push({ currStep, clusters: [C1, C2] });
  }
  currStep++;
  mergeTree.push({ currStep, clusters: H[0] });
  return mergeTree;
};

const logger = captureConsoleOutput();

const mergeTree = agglomerative(data);
console.log(JSON.stringify(mergeTree, null, 2));

const output = logger.getOutput();
logger.writeToFile("agglomerative_output.txt");
