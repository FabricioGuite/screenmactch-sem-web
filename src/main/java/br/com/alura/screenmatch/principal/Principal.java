package br.com.alura.screenmatch.principal;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.DoubleSummaryStatistics;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Scanner;
import java.util.stream.Collectors;

import br.com.alura.screenmatch.model.DadosEpisodio;
import br.com.alura.screenmatch.model.DadosSerie;
import br.com.alura.screenmatch.model.DadosTemporada;
import br.com.alura.screenmatch.model.Episodio;
import br.com.alura.screenmatch.service.ConsumoAPI;
import br.com.alura.screenmatch.service.ConverteDados;

public class Principal {


    private Scanner leitura = new Scanner(System.in);

    private final String ENDERECO = "https://www.omdbapi.com/?t=";
    private final String API_KEY = "&apikey=2f0f1eb1";

    private ConsumoAPI consumoAPI = new ConsumoAPI();
    private ConverteDados conversor = new ConverteDados();     

    public void exibeMenu(){
        //System.out.println("***********************************");
        System.out.println("Digite o nome de uma série");
        var nomeSerie = leitura.nextLine();
		var json = consumoAPI.obterDados(ENDERECO + nomeSerie.replace(" ", "+") + API_KEY);

		DadosSerie dados = conversor.obterDados(json, DadosSerie.class);
        System.out.println(dados);

        List<DadosTemporada> temporadas = new ArrayList<>();

        for (int i = 1; i <= dados.toatalTemporadas(); i++) {
        var tempJson = consumoAPI.obterDados(ENDERECO + nomeSerie.replace(" ", "+") + "&season=" + i + API_KEY);
        DadosTemporada tempDados = conversor.obterDados(tempJson, DadosTemporada.class);
        temporadas.add(tempDados);	
        }

        //temporadas.forEach(t ->t.episodios().forEach(e -> System.out.println(e.titulo())));

        List<DadosEpisodio> dadosEpisodios = temporadas.stream()
            .flatMap(t -> t.episodios().stream()) 
            .collect(Collectors.toList());

        System.out.println("\n Top 5 episódios");
        dadosEpisodios.stream().filter(e -> !e.avaliacao().equalsIgnoreCase("N/A")).
            sorted(Comparator.comparing(DadosEpisodio::avaliacao).reversed()).
            limit(5).forEach(System.out::println);
        
        System.out.println("\n");

        List<Episodio> episodios = temporadas.stream()
        .flatMap(t -> t.episodios().stream().map(d -> new Episodio(t.numero(), d))).collect(Collectors.toList());

        System.out.println("Digite o episódio que você quer buscar: ");
        var trechoTitulo = leitura.nextLine();
		
        episodios.forEach(System.out::println);

        Optional<Episodio> episodioBuscado = episodios.stream().filter(e -> e.getTitulo().toUpperCase().contains(trechoTitulo.toUpperCase())).findFirst(); 
        if(episodioBuscado.isPresent()){
            System.out.println("Episódio Encontrado!");
            System.out.println("Temporada: " + episodioBuscado.get().getTemporada());
        }else {
            System.out.println("Episódio não encontrado");
        }

        

        System.out.println("A partir de que ano você deseja ver os episódios? ");
        var ano = leitura.nextInt();
        leitura.nextLine();

        LocalDate dataBusca = LocalDate.of(ano, 1 ,1);

        DateTimeFormatter formatador = DateTimeFormatter.ofPattern("dd/MM/yyy");
        episodios.stream().filter(e -> e.getDataLancamento() != null && e.getDataLancamento()
        .isAfter(dataBusca)).forEach(e -> System.out.println("Temporada: " + e.getTemporada() +
                                                                ", Episódio: " + e.getTitulo() +
                                                                ", Data de Lançamento: " + e.getDataLancamento().format(formatador)));

        Map<Integer, Double> avaliacaoPorTemporada = episodios.stream()
                                                    .filter(e -> e.getAvaliacao() != 0.0)
                                                    .collect(Collectors.groupingBy(Episodio::getTemporada, 
                                                    Collectors.averagingDouble(Episodio::getAvaliacao)));

        System.out.println(avaliacaoPorTemporada);
//
        DoubleSummaryStatistics est = episodios.stream().filter(e -> e.getAvaliacao() != 0.0)
                                    .collect(Collectors.summarizingDouble(Episodio::getAvaliacao));

        System.out.println("Média: " + est.getAverage());
        System.out.println("Melhor Episódio: " + est.getMax());
        System.out.println("Pior Episódio: " + est.getMin());
        System.out.println("Quantidade de Episódios Considerados: " + est.getCount());
    }    
    
}
