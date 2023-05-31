import { captureConsoleOutput } from "./logger.mjs";

const data = {
  A: [1, 10],
  B: [10, 10],
  C: [1, 9.1],
  D: [5, 0],
  E: [4.5, 1],
  F: [9.1, 8.9],
  G: [2, 8.5],
  H: [5, 2],
  I: [7, 6],
  J: [2.5, 7],
  K: [8.5, 8.5],
  L: [8, 9],
};

let k = 3;
let u = [5, 10];
let v = [4, 7];
let w = [7, 7];

const dist = (p, q) => Math.hypot(p[0] - q[0], p[1] - q[1]);

const cost = (C, data, centers) =>
  +Object.entries(C)
    .reduce((prev, [k, v]) => prev + dist(data[k], centers[v]) ** 2, 0)
    .toFixed(4);

const mean = (points) =>
  [
    points.map((e) => e[0]).reduce((p, c) => p + c, 0) / points.length,
    points.map((e) => e[1]).reduce((p, c) => p + c, 0) / points.length,
  ].map((e) => +e.toFixed(4));

const kMeans = (data, centers) => {
  const clusterOf = {};
  let prevCost = 0,
    newCost = 0,
    iteration = 0;
  do {
    iteration++;
    prevCost =
      prevCost === 0 && 0 === newCost
        ? Infinity
        : cost(clusterOf, data, centers);
    Object.entries(data).forEach(([k, v]) => {
      const dists = centers.map((e) => dist(e, v));
      const minDist = Math.min(...dists);
      clusterOf[k] = dists.indexOf(minDist);
    });
    centers = centers.map((e, i) =>
      mean(
        Object.entries(clusterOf)
          .filter(([_, v]) => v === i)
          .map(([k, _]) => data[k])
      )
    );
    newCost = cost(clusterOf, data, centers);
    console.log({ iteration, prevCost, newCost, clusterOf, centers });
  } while (newCost < prevCost);

  console.log(
    "\n\n--------------------------------------------solution------------------------------------------------\n"
  );

  return { clusterOf, centers };
};

const logger = captureConsoleOutput();

console.log(kMeans(data, [u, v, w]));

const output = logger.getOutput();
logger.writeToFile("kmeans_output.txt");
