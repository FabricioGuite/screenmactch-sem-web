package br.com.alura.screenmatch.principal;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
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

        temporadas.forEach(t ->t.episodios().forEach(e -> System.out.println(e.titulo())));

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
		
        episodios.forEach(System.out::println);
    }
    
}
 